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
    
    // Map untuk menyimpan hasil wawancara per peserta ID
    private val hasilWawancaraMap = mutableMapOf<Int, com.example.commitech.data.model.HasilWawancaraResponse>()
    
    // StateFlow untuk menyimpan list semua hasil wawancara dari database (untuk tab Status)
    private val _hasilWawancaraList = MutableStateFlow<List<com.example.commitech.data.model.HasilWawancaraResponse>>(emptyList())
    val hasilWawancaraList: StateFlow<List<com.example.commitech.data.model.HasilWawancaraResponse>> = _hasilWawancaraList.asStateFlow()
    
    // State untuk loading jadwal dari database
    private val _isLoadingJadwal = MutableStateFlow(false)
    val isLoadingJadwal: StateFlow<Boolean> = _isLoadingJadwal.asStateFlow()
    
    private val _jadwalError = MutableStateFlow<String?>(null)
    val jadwalError: StateFlow<String?> = _jadwalError.asStateFlow()
    
    // Flag untuk track apakah data sudah pernah di-load
    private var isJadwalLoaded = false

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

    private val _pesertaLulusCount = MutableStateFlow(0)
    val pesertaLulusCount: StateFlow<Int> = _pesertaLulusCount.asStateFlow()

    private val _isLoadingPesertaLulusCount = MutableStateFlow(false)
    val isLoadingPesertaLulusCount: StateFlow<Boolean> = _isLoadingPesertaLulusCount.asStateFlow()

    private val _pesertaLulusCountError = MutableStateFlow<String?>(null)
    val pesertaLulusCountError: StateFlow<String?> = _pesertaLulusCountError.asStateFlow()

    private val _pesertaPendingWawancara = MutableStateFlow<List<PendaftarResponse>>(emptyList())
    val pesertaPendingWawancara: StateFlow<List<PendaftarResponse>> = _pesertaPendingWawancara.asStateFlow()

    private val _isLoadingPesertaPendingWawancara = MutableStateFlow(false)
    val isLoadingPesertaPendingWawancara: StateFlow<Boolean> = _isLoadingPesertaPendingWawancara.asStateFlow()

    private val _pesertaPendingWawancaraError = MutableStateFlow<String?>(null)
    val pesertaPendingWawancaraError: StateFlow<String?> = _pesertaPendingWawancaraError.asStateFlow()

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

    fun loadPesertaPendingWawancara(token: String?) {
        if (token == null) {
            _pesertaPendingWawancaraError.value = "Token tidak tersedia. Silakan login ulang."
            return
        }

        viewModelScope.launch {
            _isLoadingPesertaPendingWawancara.value = true
            _pesertaPendingWawancaraError.value = null

            try {
                val response = withContext(Dispatchers.IO) {
                    dataPendaftarRepository.getPesertaPendingWawancara(token)
                }

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _pesertaPendingWawancara.value = body.data
                    _isLoadingPesertaPendingWawancara.value = false
                } else {
                    _pesertaPendingWawancaraError.value = "Gagal memuat peserta pending wawancara. Status: ${response.code()}"
                    _isLoadingPesertaPendingWawancara.value = false
                }
            } catch (e: Exception) {
                _pesertaPendingWawancaraError.value = "Error: ${e.message}"
                _isLoadingPesertaPendingWawancara.value = false
            }
        }
    }

    fun loadCountPesertaLulus(token: String?) {
        if (token == null) {
            _pesertaLulusCountError.value = "Token tidak tersedia. Silakan login ulang."
            return
        }

        viewModelScope.launch {
            _isLoadingPesertaLulusCount.value = true
            _pesertaLulusCountError.value = null

            try {
                val response = withContext(Dispatchers.IO) {
                    dataPendaftarRepository.getCountPesertaLulus(token)
                }

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _pesertaLulusCount.value = body.data.count
                    _isLoadingPesertaLulusCount.value = false
                } else {
                    _pesertaLulusCountError.value = "Gagal memuat jumlah peserta lulus. Status: ${response.code()}"
                    _isLoadingPesertaLulusCount.value = false
                }
            } catch (e: Exception) {
                _pesertaLulusCountError.value = "Error: ${e.message}"
                _isLoadingPesertaLulusCount.value = false
            }
        }
    }
    
    fun loadJadwalWawancaraFromDatabase(token: String?, forceReload: Boolean = false) {
        if (token == null) {
            _jadwalError.value = "Token tidak tersedia. Silakan login ulang."
            return
        }
        
        // Jika data sudah pernah di-load dan tidak force reload, skip
        if (isJadwalLoaded && !forceReload && _days.isNotEmpty()) {
            return
        }
        
        viewModelScope.launch {
            _isLoadingJadwal.value = true
            _jadwalError.value = null
            
            try {
                val allPeserta = mutableListOf<PendaftarResponse>()
                var currentPage = 1
                var hasMore = true
                
                while (hasMore) {
                    val response = dataPendaftarRepository.getPesertaList(token, currentPage, 100)
                    
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        allPeserta.addAll(body.data)
                        val pagination = body.pagination
                        hasMore = pagination?.let { it.hasMore || it.currentPage < it.lastPage } ?: false
                        currentPage++
                    } else {
                        _jadwalError.value = "Gagal memuat jadwal wawancara. Status: ${response.code()}"
                        _isLoadingJadwal.value = false
                        return@launch
                    }
                }
                
                loadHasilWawancaraAndUpdateStatusInternal(token)
                
                var retryCount = 0
                while (hasilWawancaraMap.isEmpty() && retryCount < 3) {
                    kotlinx.coroutines.delay(100)
                    retryCount++
                }
                
                val pesertaDenganJadwal = allPeserta.filter { peserta ->
                    peserta.tanggalJadwal != null && peserta.tanggalJadwal.isNotBlank()
                }
                
                val groupedByDate = pesertaDenganJadwal.groupBy { it.tanggalJadwal!! }
                
                val dayDataList = groupedByDate.map { (tanggalJadwal, pesertaList) ->
                    val lokasi = pesertaList.firstOrNull()?.lokasi ?: "Tidak ditentukan"
                    val dayName = parseDayName(tanggalJadwal)
                    val formattedDate = formatDate(tanggalJadwal)
                    val sortedPeserta = pesertaList.sortedBy { it.waktuJadwal ?: "" }
                    
                    val participants = sortedPeserta.map { peserta ->
                        val waktuJadwal = peserta.waktuJadwal ?: "00.00"
                        val waktuFormatted = if (waktuJadwal.contains("WIB")) waktuJadwal else "$waktuJadwal WIB"
                        val statusWawancara = peserta.statusWawancara?.lowercase() ?: "pending"
                        val hasilWawancara = hasilWawancaraMap[peserta.id]
                        
                        val status = when {
                            statusWawancara == "diterima" -> InterviewStatus.ACCEPTED
                            statusWawancara == "ditolak" -> InterviewStatus.REJECTED
                            hasilWawancara?.status == "diterima" -> InterviewStatus.ACCEPTED
                            hasilWawancara?.status == "ditolak" -> InterviewStatus.REJECTED
                            else -> InterviewStatus.PENDING
                        }
                        
                        ParticipantData(
                            time = waktuFormatted,
                            name = peserta.nama ?: "Nama tidak diketahui",
                            pesertaId = peserta.id,
                            status = status,
                            division = hasilWawancara?.divisi ?: "",
                            reason = hasilWawancara?.alasan ?: "",
                            durationMinutes = 6
                        )
                    }
                    
                    DayData(
                        dayName = dayName,
                        date = formattedDate,
                        location = lokasi,
                        participants = participants
                    )
                }.sortedBy { parseDateForSorting(it.date) }
                
                _days.clear()
                _days.addAll(dayDataList)
                isJadwalLoaded = true
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
    
    fun loadHasilWawancaraAndUpdateStatus(token: String, forceReload: Boolean = false) {
        viewModelScope.launch {
            loadHasilWawancaraAndUpdateStatusInternal(token, forceReload)
        }
    }
    
    private suspend fun loadHasilWawancaraAndUpdateStatusInternal(token: String, forceReload: Boolean = false) {
        try {
            val response = hasilWawancaraRepository.getHasilWawancara(token)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.sukses && body.data != null) {
                    hasilWawancaraMap.clear()
                    body.data.forEach { hasil ->
                        hasilWawancaraMap[hasil.pesertaId] = hasil
                    }
                    _hasilWawancaraList.value = body.data
                    
                    _days.forEachIndexed { dayIndex, day ->
                        val updatedParticipants = day.participants.map { participant ->
                            val hasil = hasilWawancaraMap[participant.pesertaId]
                            if (hasil != null) {
                                val newStatus = when (hasil.status) {
                                    "diterima" -> InterviewStatus.ACCEPTED
                                    "ditolak" -> InterviewStatus.REJECTED
                                    else -> InterviewStatus.PENDING
                                }
                                participant.copy(
                                    status = newStatus,
                                    division = hasil.divisi ?: "",
                                    reason = hasil.alasan ?: ""
                                )
                            } else {
                                // Jika belum ada hasil wawancara, tetap tampilkan sebagai PENDING
                                participant
                            }
                        }
                        _days[dayIndex] = day.copy(participants = updatedParticipants)
                    }
                }
            }
        } catch (e: Exception) { }
    }
    
    private fun parseDayName(tanggal: String): String {
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
                } catch (e: Exception) { }
            }
            
            if (localDate == null) return "Hari"
            
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
                } catch (e: Exception) { }
            }
            
            if (localDate == null) return tanggal
            dateFormatter.format(localDate)
        } catch (e: Exception) {
            tanggal
        }
    }
    
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
                } catch (e: Exception) { }
            }
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
    
    fun getHasilWawancaraByStatus(statusFilter: InterviewStatus? = null): List<com.example.commitech.data.model.HasilWawancaraResponse> {
        val allHasil = _hasilWawancaraList.value
        
        if (statusFilter == null) {
            return allHasil
        }
        
        return allHasil.filter { hasil ->
            when (statusFilter) {
                InterviewStatus.ACCEPTED -> hasil.status == "diterima"
                InterviewStatus.REJECTED -> hasil.status == "ditolak"
                InterviewStatus.PENDING -> hasil.status == "pending"
            }
        }
    }
    
    fun getHasilWawancaraByPesertaId(pesertaId: Int): com.example.commitech.data.model.HasilWawancaraResponse? {
        return hasilWawancaraMap[pesertaId]
    }
    
    fun mergePesertaFromJadwalRekrutmen(
        jadwalList: List<com.example.commitech.ui.viewmodel.Jadwal>,
        pesertaPerJadwal: Map<Int, List<com.example.commitech.ui.viewmodel.Peserta>>
    ) {
        viewModelScope.launch {
            val pesertaByDate = mutableMapOf<String, MutableList<ParticipantData>>()
            
            jadwalList.forEach { jadwal ->
                if (mergedJadwalIds.contains(jadwal.id)) return@forEach
                
                val pesertaList = pesertaPerJadwal[jadwal.id] ?: emptyList()
                if (pesertaList.isEmpty()) return@forEach
                
                val tanggalMulai = jadwal.tanggalMulai
                val formattedDate = formatDate(tanggalMulai)
                val waktuMulaiParts = jadwal.waktuMulai.replace(" WIB", "").split(":", ".", " ")
                val jamMulai = waktuMulaiParts.firstOrNull()?.toIntOrNull() ?: 9
                val menitMulai = waktuMulaiParts.getOrNull(1)?.toIntOrNull() ?: 0
                
                val pesertaFiltered = pesertaList.filter { peserta ->
                    val hasilWawancara = hasilWawancaraMap[peserta.id]
                    val sudahFinal = hasilWawancara?.status == "diterima" || hasilWawancara?.status == "ditolak"
                    !sudahFinal
                }
                
                val participants = pesertaFiltered.mapIndexed { index, peserta ->
                    val menitJadwal = menitMulai + (index * 6)
                    val jamJadwal = jamMulai + (menitJadwal / 60)
                    val menitJadwalFinal = menitJadwal % 60
                    val waktuFormatted = String.format("%02d.%02d WIB", jamJadwal, menitJadwalFinal)
                    val hasilWawancara = hasilWawancaraMap[peserta.id]
                    val status = when (hasilWawancara?.status) {
                        "diterima" -> InterviewStatus.ACCEPTED
                        "ditolak" -> InterviewStatus.REJECTED
                        else -> InterviewStatus.PENDING
                    }
                    
                    ParticipantData(
                        time = waktuFormatted,
                        name = peserta.nama,
                        pesertaId = peserta.id,
                        status = status,
                        division = hasilWawancara?.divisi ?: "",
                        reason = hasilWawancara?.alasan ?: "",
                        durationMinutes = 6
                    )
                }
                
                if (!pesertaByDate.containsKey(formattedDate)) {
                    pesertaByDate[formattedDate] = mutableListOf()
                }
                pesertaByDate[formattedDate]?.addAll(participants)
                mergedJadwalIds.add(jadwal.id)
            }
            
            pesertaByDate.forEach { (formattedDate, participants) ->
                val dayName = parseDayName(formattedDate)
                val lokasi = jadwalList.firstOrNull {
                    parseDateForSorting(formatDate(it.tanggalMulai)) == parseDateForSorting(formattedDate)
                }?.lokasi?.ifBlank { "Tidak ditentukan" } ?: "Tidak ditentukan"
                
                val existingDayIndex = _days.indexOfFirst { 
                    parseDateForSorting(it.date) == parseDateForSorting(formattedDate)
                }
                
                if (existingDayIndex >= 0) {
                    val existingDay = _days[existingDayIndex]
                    val existingPesertaIds = existingDay.participants.mapNotNull { it.pesertaId }.toSet()
                    val newParticipants = participants.filter { 
                        it.pesertaId == null || it.pesertaId !in existingPesertaIds 
                    }
                    
                    if (newParticipants.isNotEmpty()) {
                        val mergedParticipants = (existingDay.participants + newParticipants).sortedBy { it.time }
                        _days[existingDayIndex] = existingDay.copy(participants = mergedParticipants)
                    }
                } else {
                    val newDay = DayData(
                        dayName = dayName,
                        date = formattedDate,
                        location = lokasi,
                        participants = participants.sortedBy { it.time }
                    )
                    
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
            
            _days.sortBy { parseDateForSorting(it.date) }
        }
    }

    private fun InterviewStatus.toBackendStatus(): String {
        return when (this) {
            InterviewStatus.ACCEPTED -> "diterima"
            InterviewStatus.REJECTED -> "ditolak"
            InterviewStatus.PENDING -> "pending"
        }
    }
    
    fun rejectWithReason(dayIndex: Int, index: Int, reason: String, token: String? = null) {
        val participant = _days.getOrNull(dayIndex)?.participants?.getOrNull(index) ?: return
        
        if (participant.pesertaId == null) {
            _saveHasilError.value = "Data peserta tidak valid. Peserta ID tidak tersedia."
            return
        }
        
        if (token == null) {
            _saveHasilError.value = "Token tidak tersedia. Silakan login ulang."
            return
        }
        
        cancelInterview(dayIndex, index)
        _isSavingHasil.value = true
        _saveHasilError.value = null
        _saveHasilSuccess.value = null
        
        viewModelScope.launch {
            try {
                val request = HasilWawancaraRequest(
                    pesertaId = participant.pesertaId!!,
                    status = InterviewStatus.REJECTED.toBackendStatus(),
                    divisi = null,
                    alasan = reason
                )
                
                val response = hasilWawancaraRepository.simpanHasilWawancara(token, request)
                
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    
                    if (responseBody.sukses && responseBody.data != null) {
                        loadHasilWawancaraAndUpdateStatusInternal(token)
                        mutateParticipant(dayIndex, index) { current ->
                            current.copy(status = InterviewStatus.REJECTED, reason = reason)
                        }
                        _saveHasilSuccess.value = "Hasil wawancara berhasil disimpan: Ditolak"
                    } else {
                        _saveHasilError.value = responseBody.pesan ?: "Gagal menyimpan hasil wawancara"
                    }
                } else {
                    val errorBody = try { response.errorBody()?.string() } catch (e: IOException) { null }
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
                _saveHasilError.value = "Tidak dapat terhubung ke server. Pastikan server berjalan."
            } catch (e: Exception) {
                _saveHasilError.value = "Terjadi kesalahan: ${e.message ?: "Unknown error"}"
            } finally {
                _isSavingHasil.value = false
            }
        }
    }

    fun acceptWithDivision(dayIndex: Int, index: Int, division: String, token: String? = null) {
        val participant = _days.getOrNull(dayIndex)?.participants?.getOrNull(index) ?: return
        
        if (participant.pesertaId == null) {
            _saveHasilError.value = "Data peserta tidak valid. Peserta ID tidak tersedia."
            return
        }
        
        if (division.isBlank()) {
            _saveHasilError.value = "Divisi harus dipilih untuk peserta yang diterima."
            return
        }
        
        if (token == null) {
            _saveHasilError.value = "Token tidak tersedia. Silakan login ulang."
            return
        }
        
        cancelInterview(dayIndex, index)
        _isSavingHasil.value = true
        _saveHasilError.value = null
        _saveHasilSuccess.value = null
        
        viewModelScope.launch {
            try {
                val request = HasilWawancaraRequest(
                    pesertaId = participant.pesertaId!!,
                    status = InterviewStatus.ACCEPTED.toBackendStatus(),
                    divisi = division,
                    alasan = null
                )
                
                val response = hasilWawancaraRepository.simpanHasilWawancara(token, request)
                
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    
                    if (responseBody.sukses && responseBody.data != null) {
                        loadHasilWawancaraAndUpdateStatusInternal(token)
                        mutateParticipant(dayIndex, index) { current ->
                            current.copy(status = InterviewStatus.ACCEPTED, division = division)
                        }
                        _saveHasilSuccess.value = "Hasil wawancara berhasil disimpan: Diterima di divisi $division"
                    } else {
                        _saveHasilError.value = responseBody.pesan ?: "Gagal menyimpan hasil wawancara"
                    }
                } else {
                    val errorBody = try { response.errorBody()?.string() } catch (e: IOException) { null }
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
                _saveHasilError.value = "Tidak dapat terhubung ke server. Pastikan server berjalan."
            } catch (e: Exception) {
                _saveHasilError.value = "Terjadi kesalahan: ${e.message ?: "Unknown error"}"
            } finally {
                _isSavingHasil.value = false
            }
        }
    }
    
    fun clearSaveHasilError() { _saveHasilError.value = null }
    fun clearSaveHasilSuccess() { _saveHasilSuccess.value = null }
    fun clearJadwalError() { _jadwalError.value = null }

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

        val currentParticipant = day.participants[participantIndex]
        val isResume = currentParticipant.remainingSeconds > 0 && 
                       currentParticipant.remainingSeconds < currentParticipant.durationMinutes * 60
        val initialRemaining = if (isResume) currentParticipant.remainingSeconds else currentParticipant.durationMinutes * 60
        val hasWarnedBefore = if (isResume) currentParticipant.warnedAtFiveMinutes else false
        val scheduleLabel = "${day.dayName}, ${day.date} • ${participant.time}"

        mutateParticipant(dayIndex, participantIndex) { current ->
            current.copy(
                isOngoing = true,
                remainingSeconds = initialRemaining,
                warnedAtFiveMinutes = hasWarnedBefore,
                hasStarted = true,
                hasCompleted = false
            )
        }

        val job = viewModelScope.launch {
            var remaining = initialRemaining
            var warningEmitted = hasWarnedBefore
            try {
                val warningSecondsBeforeEnd = 5 * 60

                val warningJob: Job? = if (initialRemaining > warningSecondsBeforeEnd) {
                    launch {
                        val delaySeconds = (initialRemaining - warningSecondsBeforeEnd).toLong()
                        delay(delaySeconds * 1_000L)

                        if (!activeInterviewJobs.containsKey(key)) return@launch

                        val currentDay = _days.getOrNull(dayIndex)
                        val currentState = currentDay?.participants?.getOrNull(participantIndex)
                        if (currentState?.warnedAtFiveMinutes == true) return@launch

                        mutateParticipant(dayIndex, participantIndex) { current ->
                            current.copy(warnedAtFiveMinutes = true)
                        }
                        _events.emit(InterviewEvent.FiveMinuteWarning(participant.name, scheduleLabel))
                    }
                } else null

                while (remaining > 0) {
                    delay(1_000L)
                    remaining -= 1

                    if (!warningEmitted && remaining <= warningSecondsBeforeEnd && remaining > 0) {
                        warningEmitted = true
                        mutateParticipant(dayIndex, participantIndex) { current ->
                            current.copy(warnedAtFiveMinutes = true)
                        }
                        _events.emit(InterviewEvent.FiveMinuteWarning(participant.name, scheduleLabel))
                    }

                    mutateParticipant(dayIndex, participantIndex) { current ->
                        current.copy(remainingSeconds = remaining, isOngoing = true, hasStarted = true)
                    }
                }

                warningJob?.cancel()
                mutateParticipant(dayIndex, participantIndex) { current ->
                    current.copy(
                        remainingSeconds = 0,
                        isOngoing = false,
                        warnedAtFiveMinutes = false,
                        hasStarted = true,
                        hasCompleted = true
                    )
                }
                _events.emit(InterviewEvent.InterviewFinished(participant.name, scheduleLabel))
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

    fun moveOrUpdateParticipantSchedule(
        dayIndex: Int,
        participantIndex: Int,
        newDate: String,
        newTime: String,
        newLocation: String
    ): Boolean {
        val currentDay = _days.getOrNull(dayIndex) ?: return false
        if (participantIndex !in currentDay.participants.indices) return false

        if (currentDay.date == newDate) {
            mutateParticipant(dayIndex, participantIndex) { current -> current.copy(time = newTime) }
            return true
        }

        val targetIndex = _days.indexOfFirst { it.date == newDate }
        if (targetIndex == -1) return false

        val sourceParticipants = currentDay.participants.toMutableList()
        val participant = sourceParticipants.removeAt(participantIndex).copy(time = newTime)
        _days[dayIndex] = currentDay.copy(participants = sourceParticipants)

        val targetDay = _days[targetIndex]
        val targetParticipants = targetDay.participants.toMutableList()
        targetParticipants.add(participant)
        _days[targetIndex] = targetDay.copy(participants = targetParticipants)

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

