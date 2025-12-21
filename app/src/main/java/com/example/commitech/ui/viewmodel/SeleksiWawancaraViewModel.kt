package com.example.commitech.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.commitech.data.model.HasilWawancaraRequest
import com.example.commitech.data.model.PendaftarResponse
import com.example.commitech.data.repository.DataPendaftarRepository
import com.example.commitech.data.repository.HasilWawancaraRepository
import java.io.IOException
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data class untuk representasi peserta dalam jadwal wawancara
 * 
 * @property time Waktu wawancara (contoh: "07.00 WIB")
 * @property name Nama peserta
 * @property pesertaId ID peserta di database (nullable, diperlukan untuk API call)
 *                      Jika null, berarti peserta belum terdaftar di database
 * @property status Status hasil wawancara (PENDING, ACCEPTED, REJECTED)
 * @property reason Alasan penolakan (jika status = REJECTED)
 * @property division Nama divisi (jika status = ACCEPTED)
 * @property durationMinutes Durasi wawancara dalam menit (default: 60)
 * @property isOngoing Flag apakah wawancara sedang berlangsung
 * @property remainingSeconds Sisa waktu wawancara dalam detik
 * @property warnedAtFiveMinutes Flag apakah sudah ada peringatan 5 menit sebelum selesai
 * @property hasStarted Flag apakah wawancara sudah dimulai
 * @property hasCompleted Flag apakah wawancara sudah selesai
 */
data class ParticipantData(
    val time: String,
    val name: String,
    val pesertaId: Int? = null,  // ID peserta dari database (untuk API call)
    var status: InterviewStatus = InterviewStatus.PENDING,
    var reason: String = "",
    var division: String = "",
    var durationMinutes: Int = 6,  // Durasi wawancara 6 menit
    var isOngoing: Boolean = false,
    var remainingSeconds: Int = durationMinutes * 60,
    var warnedAtFiveMinutes: Boolean = false,
    var hasStarted: Boolean = false,
    var hasCompleted: Boolean = false
)

data class DayData(
    val dayName: String,
    val date: String,
    val location: String,
    val participants: List<ParticipantData>
)

sealed class InterviewEvent {
    data class FiveMinuteWarning(
        val participantName: String,
        val scheduleLabel: String
    ) : InterviewEvent()

    data class InterviewFinished(
        val participantName: String,
        val scheduleLabel: String
    ) : InterviewEvent()
}

data class ReminderSchedule(
    val key: String,
    val triggerAtMillis: Long,
    val participantName: String,
    val scheduleLabel: String
)

/**
 * ViewModel untuk mengelola state dan logika bisnis seleksi wawancara
 * 
 * Fitur yang dikelola:
 * - Menampilkan jadwal wawancara per hari
 * - Timer wawancara dengan notifikasi
 * - Input hasil wawancara (Terima/Tolak) dengan integrasi backend API
 * - Reminder notifikasi untuk jadwal wawancara
 */
class SeleksiWawancaraViewModel : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID"))
    private val timeFormatter = DateTimeFormatter.ofPattern("HH.mm", Locale("id", "ID"))

    // Repository untuk API call
    private val hasilWawancaraRepository = HasilWawancaraRepository()
    private val dataPendaftarRepository = DataPendaftarRepository()

    private val _days = mutableStateListOf<DayData>()
    val days: List<DayData> get() = _days
    
    // Track jadwal yang sudah di-merge untuk menghindari duplikasi
    private val mergedJadwalIds = mutableSetOf<Int>()
    
    // State untuk loading jadwal dari database
    private val _isLoadingJadwal = MutableStateFlow(false)
    val isLoadingJadwal: StateFlow<Boolean> = _isLoadingJadwal.asStateFlow()
    
    private val _jadwalError = MutableStateFlow<String?>(null)
    val jadwalError: StateFlow<String?> = _jadwalError.asStateFlow()

    private val activeInterviewJobs = mutableMapOf<String, Job>()
    private val scheduledReminderKeys = mutableSetOf<String>()

    private val _events = MutableSharedFlow<InterviewEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<InterviewEvent> = _events.asSharedFlow()
    
    // State untuk API call hasil wawancara
    private val _isSavingHasil = MutableStateFlow(false)
    val isSavingHasil: StateFlow<Boolean> = _isSavingHasil.asStateFlow()
    
    private val _saveHasilError = MutableStateFlow<String?>(null)
    val saveHasilError: StateFlow<String?> = _saveHasilError.asStateFlow()
    
    private val _saveHasilSuccess = MutableStateFlow<String?>(null)
    val saveHasilSuccess: StateFlow<String?> = _saveHasilSuccess.asStateFlow()
    
    // State untuk peserta lulus tanpa jadwal
    private val _pesertaLulusTanpaJadwal = mutableStateListOf<PendaftarResponse>()
    val pesertaLulusTanpaJadwal: List<PendaftarResponse> get() = _pesertaLulusTanpaJadwal
    
    private val _isLoadingPesertaLulus = MutableStateFlow(false)
    val isLoadingPesertaLulus: StateFlow<Boolean> = _isLoadingPesertaLulus.asStateFlow()
    
    private val _pesertaLulusError = MutableStateFlow<String?>(null)
    val pesertaLulusError: StateFlow<String?> = _pesertaLulusError.asStateFlow()

    init {
        // Init kosong - data akan di-load dari database saat screen dibuka
        // Mock data dihapus, diganti dengan load dari database
    }
    
    /**
     * Load peserta yang lulus seleksi berkas tapi belum ada jadwal wawancara
     */
    fun loadPesertaLulusTanpaJadwal(token: String?) {
        if (token == null) {
            _pesertaLulusError.value = "Token tidak tersedia. Silakan login ulang."
            return
        }
        
        viewModelScope.launch {
            _isLoadingPesertaLulus.value = true
            _pesertaLulusError.value = null
            
            try {
                val response = withContext(Dispatchers.IO) {
                    dataPendaftarRepository.getPesertaLulusTanpaJadwal(token)
                }
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _pesertaLulusTanpaJadwal.clear()
                    _pesertaLulusTanpaJadwal.addAll(body.data)
                    _isLoadingPesertaLulus.value = false
                } else {
                    _pesertaLulusError.value = "Gagal memuat peserta lulus tanpa jadwal. Status: ${response.code()}"
                    _isLoadingPesertaLulus.value = false
                }
            } catch (e: Exception) {
                _pesertaLulusError.value = "Error: ${e.message}"
                _isLoadingPesertaLulus.value = false
            }
        }
    }
    
    /**
     * Load jadwal wawancara dari database
     * 
     * Fungsi ini akan:
     * 1. Mengambil semua peserta yang memiliki jadwal (tanggal_jadwal tidak null)
     * 2. Group peserta berdasarkan tanggal_jadwal
     * 3. Map ke struktur DayData untuk ditampilkan di UI
     * 
     * @param token Token autentikasi untuk API call
     */
    fun loadJadwalWawancaraFromDatabase(token: String?) {
        if (token == null) {
            _jadwalError.value = "Token tidak tersedia. Silakan login ulang."
            return
        }
        
        viewModelScope.launch {
            _isLoadingJadwal.value = true
            _jadwalError.value = null
            
            try {
                // Load semua peserta dengan pagination
                // Kita load semua data tanpa pagination untuk mendapatkan semua jadwal
                val allPeserta = mutableListOf<PendaftarResponse>()
                var currentPage = 1
                var hasMore = true
                
                while (hasMore) {
                    val response = dataPendaftarRepository.getPesertaList(token, currentPage, 100) // Load 100 per page untuk mengurangi API call
                    
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        allPeserta.addAll(body.data)
                        
                        // Check if there's more pages
                        val pagination = body.pagination
                        hasMore = pagination?.let { 
                            it.hasMore || it.currentPage < it.lastPage 
                        } ?: false
                        currentPage++
                    } else {
                        _jadwalError.value = "Gagal memuat jadwal wawancara. Status: ${response.code()}"
                        _isLoadingJadwal.value = false
                        return@launch
                    }
                }
                
                // Filter peserta yang memiliki jadwal (tanggal_jadwal tidak null)
                val pesertaDenganJadwal = allPeserta.filter { 
                    it.tanggalJadwal != null && it.tanggalJadwal.isNotBlank()
                }
                
                // Group peserta berdasarkan tanggal_jadwal
                val groupedByDate = pesertaDenganJadwal.groupBy { it.tanggalJadwal!! }
                
                // Map ke DayData
                val dayDataList = groupedByDate.map { (tanggalJadwal, pesertaList) ->
                    // Ambil lokasi dari peserta pertama (asumsi semua peserta di tanggal yang sama punya lokasi yang sama)
                    val lokasi = pesertaList.firstOrNull()?.lokasi ?: "Tidak ditentukan"
                    
                    // Parse tanggal untuk mendapatkan nama hari
                    val dayName = parseDayName(tanggalJadwal)
                    val formattedDate = formatDate(tanggalJadwal)
                    
                    // Sort peserta berdasarkan waktu_jadwal
                    val sortedPeserta = pesertaList.sortedBy { it.waktuJadwal ?: "" }
                    
                    // Map peserta ke ParticipantData
                    val participants = sortedPeserta.map { peserta ->
                        val waktuJadwal = peserta.waktuJadwal ?: "00.00"
                        val waktuFormatted = if (waktuJadwal.contains("WIB")) {
                            waktuJadwal
                        } else {
                            "$waktuJadwal WIB"
                        }
                        
                        ParticipantData(
                            time = waktuFormatted,
                            name = peserta.nama ?: "Nama tidak diketahui",
                            pesertaId = peserta.id,
                            status = InterviewStatus.PENDING, // Default status
                            durationMinutes = 6  // Durasi wawancara 6 menit
                        )
                    }
                    
                    DayData(
                        dayName = dayName,
                        date = formattedDate,
                        location = lokasi,
                        participants = participants
                    )
                }.sortedBy { 
                    // Sort berdasarkan tanggal (tertua dulu)
                    parseDateForSorting(it.date)
                }
                
                // Update days list
                _days.clear()
                _days.addAll(dayDataList)
                
                _isLoadingJadwal.value = false
            } catch (e: IOException) {
                _jadwalError.value = "Tidak dapat terhubung ke server. Pastikan server berjalan."
                _isLoadingJadwal.value = false
            } catch (e: Exception) {
                _jadwalError.value = "Terjadi kesalahan: ${e.message ?: "Unknown error"}"
                _isLoadingJadwal.value = false
            }
        }
    }
    
    /**
     * Parse nama hari dari tanggal (format: "d MMM yyyy" atau format lain)
     */
    private fun parseDayName(tanggal: String): String {
        return try {
            // Coba berbagai format tanggal
            val formats = listOf(
                DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID")),
                DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("id", "ID")),
                DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale("id", "ID")),
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID")),
                DateTimeFormatter.ISO_LOCAL_DATE
            )
            
            var localDate: LocalDate? = null
            for (format in formats) {
                try {
                    localDate = LocalDate.parse(tanggal, format)
                    break
                } catch (e: Exception) {
                    // Continue to next format
                }
            }
            
            if (localDate == null) {
                // Jika tidak bisa parse, gunakan format default
                return "Hari"
            }
            
            val dayOfWeek = localDate.dayOfWeek
            when (dayOfWeek.value) {
                1 -> "Senin"
                2 -> "Selasa"
                3 -> "Rabu"
                4 -> "Kamis"
                5 -> "Jumat"
                6 -> "Sabtu"
                7 -> "Minggu"
                else -> "Hari"
            }
        } catch (e: Exception) {
            "Hari"
        }
    }
    
    /**
     * Format tanggal ke format "d MMM yyyy" untuk ditampilkan
     */
    private fun formatDate(tanggal: String): String {
        return try {
            val formats = listOf(
                DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID")),
                DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("id", "ID")),
                DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale("id", "ID")),
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID")),
                DateTimeFormatter.ISO_LOCAL_DATE
            )
            
            var localDate: LocalDate? = null
            for (format in formats) {
                try {
                    localDate = LocalDate.parse(tanggal, format)
                    break
                } catch (e: Exception) {
                    // Continue to next format
                }
            }
            
            if (localDate == null) {
                // Jika tidak bisa parse, return as is
                return tanggal
            }
            
            dateFormatter.format(localDate)
        } catch (e: Exception) {
            tanggal
        }
    }
    
    /**
     * Parse tanggal untuk sorting (mengembalikan LocalDate untuk perbandingan)
     */
    private fun parseDateForSorting(tanggal: String): LocalDate {
        return try {
            val formats = listOf(
                DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID")),
                DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("id", "ID")),
                DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale("id", "ID")),
                DateTimeFormatter.ISO_LOCAL_DATE
            )
            
            for (format in formats) {
                try {
                    return LocalDate.parse(tanggal, format)
                } catch (e: Exception) {
                    // Continue to next format
                }
            }
            
            // Default jika tidak bisa parse
            LocalDate.now()
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    fun updateParticipant(
        dayIndex: Int,
        participantIndex: Int,
        newDate: String,
        newTime: String,
        newLocation: String
    ) {
        cancelInterview(dayIndex, participantIndex)
        unregisterReminder(dayIndex, participantIndex)
        mutateParticipant(dayIndex, participantIndex) { current ->
            current.copy(
                time = newTime,
                remainingSeconds = current.durationMinutes * 60,
                warnedAtFiveMinutes = false,
                isOngoing = false,
                hasStarted = false,
                hasCompleted = false
            )
        }
        val day = _days.getOrNull(dayIndex) ?: return
        _days[dayIndex] = day.copy(date = newDate, location = newLocation, participants = day.participants)
    }

    fun totalParticipants(): Int = _days.sumOf { it.participants.size }

    fun getAllParticipants(): List<ParticipantData> = days.flatMap { it.participants }
    
    /**
     * Merge peserta dari jadwal rekrutmen ke dalam days
     * Fungsi ini akan mengkonversi peserta dari JadwalViewModel ke format DayData dan ParticipantData
     * 
     * @param jadwalList List jadwal rekrutmen
     * @param pesertaPerJadwal Map jadwalId ke List<Peserta>
     */
    fun mergePesertaFromJadwalRekrutmen(
        jadwalList: List<com.example.commitech.ui.viewmodel.Jadwal>,
        pesertaPerJadwal: Map<Int, List<com.example.commitech.ui.viewmodel.Peserta>>
    ) {
        viewModelScope.launch {
            // Buat map untuk menyimpan peserta per tanggal
            val pesertaByDate = mutableMapOf<String, MutableList<ParticipantData>>()
            
            // Iterate semua jadwal dan peserta
            jadwalList.forEach { jadwal ->
                // Skip jika jadwal ini sudah di-merge sebelumnya
                if (mergedJadwalIds.contains(jadwal.id)) return@forEach
                
                val pesertaList = pesertaPerJadwal[jadwal.id] ?: emptyList()
                
                if (pesertaList.isEmpty()) return@forEach
                
                // Parse tanggal mulai untuk grouping
                val tanggalMulai = jadwal.tanggalMulai
                val formattedDate = formatDate(tanggalMulai)
                
                // Parse waktu mulai jadwal
                val waktuMulaiParts = jadwal.waktuMulai.replace(" WIB", "").split(":", ".", " ")
                val jamMulai = waktuMulaiParts.firstOrNull()?.toIntOrNull() ?: 9
                val menitMulai = waktuMulaiParts.getOrNull(1)?.toIntOrNull() ?: 0
                
                // Konversi peserta ke ParticipantData dengan waktu yang benar per jadwal
                val participants = pesertaList.mapIndexed { index, peserta ->
                    // Hitung waktu untuk peserta ini (setiap 6 menit dari waktu mulai jadwal)
                    val menitJadwal = menitMulai + (index * 6)
                    val jamJadwal = jamMulai + (menitJadwal / 60)
                    val menitJadwalFinal = menitJadwal % 60
                    val waktuFormatted = String.format("%02d.%02d WIB", jamJadwal, menitJadwalFinal)
                    
                    ParticipantData(
                        time = waktuFormatted,
                        name = peserta.nama,
                        pesertaId = peserta.id,
                        status = InterviewStatus.PENDING,
                        durationMinutes = 6
                    )
                }
                
                // Tambahkan ke map per tanggal
                if (!pesertaByDate.containsKey(formattedDate)) {
                    pesertaByDate[formattedDate] = mutableListOf()
                }
                pesertaByDate[formattedDate]?.addAll(participants)
                
                // Mark jadwal ini sudah di-merge
                mergedJadwalIds.add(jadwal.id)
            }
            
            // Merge dengan days yang sudah ada
            pesertaByDate.forEach { (formattedDate, participants) ->
                // Parse tanggal untuk mendapatkan nama hari
                val dayName = parseDayName(formattedDate)
                val lokasi = "Sekretariat BEM KM FTI" // Default lokasi
                
                // Cari day yang sudah ada dengan tanggal yang sama
                val existingDayIndex = _days.indexOfFirst { 
                    parseDateForSorting(it.date) == parseDateForSorting(formattedDate)
                }
                
                if (existingDayIndex >= 0) {
                    // Merge dengan day yang sudah ada, hanya tambahkan peserta yang belum ada
                    val existingDay = _days[existingDayIndex]
                    val existingPesertaIds = existingDay.participants.mapNotNull { it.pesertaId }.toSet()
                    val newParticipants = participants.filter { 
                        it.pesertaId == null || it.pesertaId !in existingPesertaIds 
                    }
                    
                    if (newParticipants.isNotEmpty()) {
                        val mergedParticipants = (existingDay.participants + newParticipants)
                            .sortedBy { it.time }
                        
                        _days[existingDayIndex] = existingDay.copy(
                            participants = mergedParticipants
                        )
                    }
                } else {
                    // Buat day baru
                    val newDay = DayData(
                        dayName = dayName,
                        date = formattedDate,
                        location = lokasi,
                        participants = participants.sortedBy { it.time }
                    )
                    
                    // Insert ke posisi yang tepat (sorted by date)
                    val insertIndex = _days.indexOfFirst { 
                        parseDateForSorting(it.date) > parseDateForSorting(formattedDate)
                    }
                    
                    if (insertIndex >= 0) {
                        _days.add(insertIndex, newDay)
                    } else {
                        _days.add(newDay)
                    }
                }
            }
            
            // Re-sort days berdasarkan tanggal
            _days.sortBy { parseDateForSorting(it.date) }
        }
    }

    /**
     * Helper function: Convert InterviewStatus enum ke string format backend
     * 
     * @param status InterviewStatus enum (PENDING, ACCEPTED, REJECTED)
     * @return String format backend ("pending", "diterima", "ditolak")
     */
    private fun InterviewStatus.toBackendStatus(): String {
        return when (this) {
            InterviewStatus.ACCEPTED -> "diterima"
            InterviewStatus.REJECTED -> "ditolak"
            InterviewStatus.PENDING -> "pending"
        }
    }
    
    /**
     * Menolak peserta dengan alasan dan menyimpan ke backend API
     * 
     * Fitur: Modul 4 - Fitur 16: Input Hasil Wawancara (Reject)
     * 
     * Flow:
     * 1. Validasi pesertaId (harus ada)
     * 2. Call API untuk save hasil wawancara
     * 3. Jika berhasil: update local state
     * 4. Jika error: tampilkan error, jangan update local state
     * 
     * @param dayIndex Index hari dalam jadwal
     * @param index Index peserta dalam hari tersebut
     * @param reason Alasan penolakan
     * @param token Token autentikasi (dari AuthViewModel)
     */
    fun rejectWithReason(dayIndex: Int, index: Int, reason: String, token: String? = null) {
        val participant = _days.getOrNull(dayIndex)?.participants?.getOrNull(index)
            ?: return
        
        // Validasi pesertaId
        if (participant.pesertaId == null) {
            _saveHasilError.value = "Data peserta tidak valid. Peserta ID tidak tersedia."
            return
        }
        
        // Validasi token
        if (token == null) {
            _saveHasilError.value = "Token tidak tersedia. Silakan login ulang."
            return
        }
        
        // Cancel interview timer
        cancelInterview(dayIndex, index)
        
        // Set loading state
        _isSavingHasil.value = true
        _saveHasilError.value = null
        _saveHasilSuccess.value = null
        
        viewModelScope.launch {
            try {
                // Build request untuk API
                val request = HasilWawancaraRequest(
                    pesertaId = participant.pesertaId!!,
                    status = InterviewStatus.REJECTED.toBackendStatus(),
                    divisi = null,
                    alasan = reason
                )
                
                // Call API
                val response = hasilWawancaraRepository.simpanHasilWawancara(token, request)
                
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    
                    if (responseBody.sukses && responseBody.data != null) {
                        // Success: Update local state
                        mutateParticipant(dayIndex, index) { current ->
                            current.copy(
                                status = InterviewStatus.REJECTED,
                                reason = reason
                            )
                        }
                        _saveHasilSuccess.value = "Hasil wawancara berhasil disimpan: Ditolak"
                    } else {
                        // Backend return sukses=false
                        _saveHasilError.value = responseBody.pesan ?: "Gagal menyimpan hasil wawancara"
                    }
                } else {
                    // HTTP Error (400, 422, 500, dll)
                    val errorBody = try {
                        response.errorBody()?.string()
                    } catch (e: IOException) {
                        null
                    }
                    
                    val errorMessage = when (response.code()) {
                        400 -> "Hasil wawancara untuk peserta ini sudah ada. Gunakan fitur ubah hasil."
                        422 -> "Data tidak valid. ${errorBody ?: "Silakan coba lagi."}"
                        401 -> "Token tidak valid. Silakan login ulang."
                        500 -> "Terjadi kesalahan di server. Silakan coba lagi nanti."
                        else -> errorBody ?: "Gagal menyimpan hasil wawancara. Status: ${response.code()}"
                    }
                    
                    _saveHasilError.value = errorMessage
                }
            } catch (e: IOException) {
                // Network error
                _saveHasilError.value = "Tidak dapat terhubung ke server. Pastikan server berjalan."
            } catch (e: Exception) {
                // Unexpected error
                _saveHasilError.value = "Terjadi kesalahan: ${e.message ?: "Unknown error"}"
            } finally {
                _isSavingHasil.value = false
            }
        }
    }

    /**
     * Menerima peserta dengan divisi dan menyimpan ke backend API
     * 
     * Fitur: Modul 4 - Fitur 16: Input Hasil Wawancara (Accept)
     * 
     * Flow:
     * 1. Validasi pesertaId (harus ada)
     * 2. Validasi divisi (harus diisi jika diterima)
     * 3. Call API untuk save hasil wawancara
     * 4. Jika berhasil: update local state
     * 5. Jika error: tampilkan error, jangan update local state
     * 
     * @param dayIndex Index hari dalam jadwal
     * @param index Index peserta dalam hari tersebut
     * @param division Nama divisi yang diterima
     * @param token Token autentikasi (dari AuthViewModel)
     */
    fun acceptWithDivision(dayIndex: Int, index: Int, division: String, token: String? = null) {
        val participant = _days.getOrNull(dayIndex)?.participants?.getOrNull(index)
            ?: return
        
        // Validasi pesertaId
        if (participant.pesertaId == null) {
            _saveHasilError.value = "Data peserta tidak valid. Peserta ID tidak tersedia."
            return
        }
        
        // Validasi divisi
        if (division.isBlank()) {
            _saveHasilError.value = "Divisi harus dipilih untuk peserta yang diterima."
            return
        }
        
        // Validasi token
        if (token == null) {
            _saveHasilError.value = "Token tidak tersedia. Silakan login ulang."
            return
        }
        
        // Cancel interview timer
        cancelInterview(dayIndex, index)
        
        // Set loading state
        _isSavingHasil.value = true
        _saveHasilError.value = null
        _saveHasilSuccess.value = null
        
        viewModelScope.launch {
            try {
                // Build request untuk API
                val request = HasilWawancaraRequest(
                    pesertaId = participant.pesertaId!!,
                    status = InterviewStatus.ACCEPTED.toBackendStatus(),
                    divisi = division,
                    alasan = null
                )
                
                // Call API
                val response = hasilWawancaraRepository.simpanHasilWawancara(token, request)
                
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    
                    if (responseBody.sukses && responseBody.data != null) {
                        // Success: Update local state
                        mutateParticipant(dayIndex, index) { current ->
                            current.copy(
                                status = InterviewStatus.ACCEPTED,
                                division = division
                            )
                        }
                        _saveHasilSuccess.value = "Hasil wawancara berhasil disimpan: Diterima di divisi $division"
                    } else {
                        // Backend return sukses=false
                        _saveHasilError.value = responseBody.pesan ?: "Gagal menyimpan hasil wawancara"
                    }
                } else {
                    // HTTP Error (400, 422, 500, dll)
                    val errorBody = try {
                        response.errorBody()?.string()
                    } catch (e: IOException) {
                        null
                    }
                    
                    val errorMessage = when (response.code()) {
                        400 -> "Hasil wawancara untuk peserta ini sudah ada. Gunakan fitur ubah hasil."
                        422 -> "Data tidak valid. ${errorBody ?: "Silakan coba lagi."}"
                        401 -> "Token tidak valid. Silakan login ulang."
                        500 -> "Terjadi kesalahan di server. Silakan coba lagi nanti."
                        else -> errorBody ?: "Gagal menyimpan hasil wawancara. Status: ${response.code()}"
                    }
                    
                    _saveHasilError.value = errorMessage
                }
            } catch (e: IOException) {
                // Network error
                _saveHasilError.value = "Tidak dapat terhubung ke server. Pastikan server berjalan."
            } catch (e: Exception) {
                // Unexpected error
                _saveHasilError.value = "Terjadi kesalahan: ${e.message ?: "Unknown error"}"
            } finally {
                _isSavingHasil.value = false
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearSaveHasilError() {
        _saveHasilError.value = null
    }
    
    /**
     * Clear success message
     */
    fun clearSaveHasilSuccess() {
        _saveHasilSuccess.value = null
    }
    
    /**
     * Clear error message untuk jadwal wawancara
     */
    fun clearJadwalError() {
        _jadwalError.value = null
    }

    fun cancelInterview(dayIndex: Int, participantIndex: Int) {
        stopActiveInterview(dayIndex, participantIndex)
        mutateParticipant(dayIndex, participantIndex) { current ->
            current.copy(
                isOngoing = false,
                remainingSeconds = current.durationMinutes * 60,
                warnedAtFiveMinutes = false,
                hasStarted = false,
                hasCompleted = false
            )
        }
    }

    fun startInterview(dayIndex: Int, participantIndex: Int) {
        val day = _days.getOrNull(dayIndex) ?: return
        if (participantIndex !in day.participants.indices) return

        val participant = day.participants[participantIndex]
        val key = participantKey(dayIndex, participantIndex)
        if (activeInterviewJobs.containsKey(key)) return

        mutateParticipant(dayIndex, participantIndex) { current ->
            current.copy(
                isOngoing = true,
                remainingSeconds = current.durationMinutes * 60,
                warnedAtFiveMinutes = false,
                hasStarted = true,
                hasCompleted = false
            )
        }

        val scheduleLabel = "${day.dayName}, ${day.date} • ${participant.time}"

        val job = viewModelScope.launch {
            var remaining = participant.durationMinutes * 60
            var warned = false
            try {
                val warningSecondsBeforeEnd = 5 * 60  // Warning saat tersisa 5 menit (300 detik) dari total 6 menit
                while (remaining > 0) {
                    delay(1_000L)
                    remaining -= 1
                    val shouldWarn = !warned &&
                        remaining == warningSecondsBeforeEnd
                    mutateParticipant(dayIndex, participantIndex) { current ->
                        current.copy(
                            remainingSeconds = remaining,
                            warnedAtFiveMinutes = shouldWarn || current.warnedAtFiveMinutes,
                            isOngoing = true,
                            hasStarted = true
                        )
                    }
                    if (!warned && shouldWarn) {
                        warned = true
                        _events.emit(
                            InterviewEvent.FiveMinuteWarning(
                                participantName = participant.name,
                                scheduleLabel = scheduleLabel
                            )
                        )
                    }
                }
                mutateParticipant(dayIndex, participantIndex) { current ->
                    current.copy(
                        remainingSeconds = 0,
                        isOngoing = false,
                        warnedAtFiveMinutes = false,
                        hasStarted = true,
                        hasCompleted = true
                    )
                }
                _events.emit(
                    InterviewEvent.InterviewFinished(
                        participantName = participant.name,
                        scheduleLabel = scheduleLabel
                    )
                )
            } catch (cancel: CancellationException) {
                throw cancel
            } finally {
                activeInterviewJobs.remove(key)
            }
        }

        activeInterviewJobs[key] = job
    }

    fun stopInterview(dayIndex: Int, participantIndex: Int) {
        stopActiveInterview(dayIndex, participantIndex)
        mutateParticipant(dayIndex, participantIndex) { current ->
            current.copy(
                isOngoing = false,
                remainingSeconds = 0,
                warnedAtFiveMinutes = false,
                hasStarted = true,
                hasCompleted = true
            )
        }
    }

    private fun stopActiveInterview(dayIndex: Int, participantIndex: Int) {
        val key = participantKey(dayIndex, participantIndex)
        activeInterviewJobs.remove(key)?.cancel()
    }

    fun buildReminderSchedule(dayIndex: Int, participantIndex: Int): ReminderSchedule? {
        val day = _days.getOrNull(dayIndex) ?: return null
        if (participantIndex !in day.participants.indices) return null
        val participant = day.participants[participantIndex]
        val interviewInstant = parseInterviewInstant(day, participant) ?: return null
        val triggerInstant = interviewInstant.minus(Duration.ofMinutes(10))
        val triggerMillis = triggerInstant.toEpochMilli()
        if (triggerMillis <= System.currentTimeMillis()) return null
        val key = participantKey(dayIndex, participantIndex)
        val scheduleLabel = "${day.dayName}, ${day.date} • ${participant.time}"
        return ReminderSchedule(
            key = key,
            triggerAtMillis = triggerMillis,
            participantName = participant.name,
            scheduleLabel = scheduleLabel
        )
    }

    fun registerReminder(schedule: ReminderSchedule): Boolean {
        return scheduledReminderKeys.add(schedule.key)
    }

    fun updateParticipantByName(name: String, newStatus: InterviewStatus, newDivision: String) {
        for (dayIndex in _days.indices) {
            val day = _days[dayIndex]
            val participantIndex = day.participants.indexOfFirst { it.name == name }
            if (participantIndex != -1) {
                cancelInterview(dayIndex, participantIndex)
                mutateParticipant(dayIndex, participantIndex) { current ->
                    current.copy(
                        status = newStatus,
                        division = if (newStatus == InterviewStatus.ACCEPTED) newDivision else "",
                        reason = if (newStatus == InterviewStatus.REJECTED) "Diubah menjadi ditolak" else ""
                    )
                }
                break
            }
        }
    }

    /**
     * Pindahkan peserta ke tanggal lain bila ada di jadwal,
     * atau jika tanggal sama hanya ubah jamnya. Tidak mengubah tanggal/loc pada kartu hari (DayData).
     * Mengembalikan true jika berhasil, false bila tanggal target tidak ada.
     */
    fun moveOrUpdateParticipantSchedule(
        dayIndex: Int,
        participantIndex: Int,
        newDate: String,
        newTime: String,
        newLocation: String
    ): Boolean {
        val currentDay = _days.getOrNull(dayIndex) ?: return false
        if (participantIndex !in currentDay.participants.indices) return false

        // Jika tetap di hari yang sama, hanya ubah jam (time)
        if (currentDay.date == newDate) {
            mutateParticipant(dayIndex, participantIndex) { current ->
                current.copy(time = newTime)
            }
            return true
        }

        // Cari hari tujuan berdasarkan tanggal
        val targetIndex = _days.indexOfFirst { it.date == newDate }
        if (targetIndex == -1) {
            return false
        }

        // Pindahkan peserta
        val sourceParticipants = currentDay.participants.toMutableList()
        val participant = sourceParticipants.removeAt(participantIndex).copy(time = newTime)
        _days[dayIndex] = currentDay.copy(participants = sourceParticipants)

        val targetDay = _days[targetIndex]
        val targetParticipants = targetDay.participants.toMutableList()
        targetParticipants.add(participant)
        _days[targetIndex] = targetDay.copy(participants = targetParticipants)

        // Batalkan timer/reminder peserta yang dipindahkan
        stopActiveInterview(dayIndex, participantIndex)
        return true
    }

    private fun participantKey(dayIndex: Int, participantIndex: Int): String {
        val day = _days.getOrNull(dayIndex)
        val participant = day?.participants?.getOrNull(participantIndex)
        return listOfNotNull(day?.dayName, day?.date, participant?.name, participant?.time)
            .joinToString(separator = "|")
    }

    private fun unregisterReminder(dayIndex: Int, participantIndex: Int) {
        val key = participantKey(dayIndex, participantIndex)
        scheduledReminderKeys.remove(key)
    }

    private fun mutateParticipant(
        dayIndex: Int,
        participantIndex: Int,
        block: (ParticipantData) -> ParticipantData
    ) {
        if (dayIndex !in _days.indices) return
        val day = _days[dayIndex]
        if (participantIndex !in day.participants.indices) return
        val updatedList = day.participants.toMutableList()
        val current = updatedList[participantIndex]
        updatedList[participantIndex] = block(current)
        _days[dayIndex] = day.copy(participants = updatedList)
    }

    private fun parseInterviewInstant(day: DayData, participant: ParticipantData): java.time.Instant? {
        return runCatching {
            val localDate = LocalDate.parse(day.date, dateFormatter)
            val cleanedTime = participant.time.replace(" WIB", "", ignoreCase = true).trim()
            val localTime = LocalTime.parse(cleanedTime, timeFormatter)
            val localDateTime = LocalDateTime.of(localDate, localTime)
            localDateTime.atZone(ZoneId.systemDefault()).toInstant()
        }.getOrNull()
    }
}

