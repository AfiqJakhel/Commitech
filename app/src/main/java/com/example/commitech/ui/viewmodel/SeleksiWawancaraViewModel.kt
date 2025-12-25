package com.example.commitech.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.commitech.data.model.PendaftarResponse
import com.example.commitech.data.repository.DataPendaftarRepository
import com.example.commitech.data.repository.HasilWawancaraRepository
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ParticipantData(
    val time: String,
    val name: String,
    val pesertaId: Int? = null,
    var status: InterviewStatus = InterviewStatus.PENDING,
    var reason: String = "",
    var division: String = "",
    var durationMinutes: Int = 6,
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

class SeleksiWawancaraViewModel : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID"))

    private val hasilWawancaraRepository = HasilWawancaraRepository()
    private val dataPendaftarRepository = DataPendaftarRepository()

    private val _days = mutableStateListOf<DayData>()
    val days: List<DayData> get() = _days

    private val hasilWawancaraMap = mutableMapOf<Int, com.example.commitech.data.model.HasilWawancaraResponse>()

    private val _hasilWawancaraList = MutableStateFlow<List<com.example.commitech.data.model.HasilWawancaraResponse>>(emptyList())
    val hasilWawancaraList: StateFlow<List<com.example.commitech.data.model.HasilWawancaraResponse>> = _hasilWawancaraList.asStateFlow()

    private val _isLoadingJadwal = MutableStateFlow(false)
    val isLoadingJadwal: StateFlow<Boolean> = _isLoadingJadwal.asStateFlow()
    
    private val _jadwalError = MutableStateFlow<String?>(null)

    private var isJadwalLoaded = false

    private val activeInterviewJobs = mutableMapOf<String, Job>()

    private val _isSavingHasil = MutableStateFlow(false)
    val isSavingHasil: StateFlow<Boolean> = _isSavingHasil.asStateFlow()
    
    private val _saveHasilError = MutableStateFlow<String?>(null)
    val saveHasilError: StateFlow<String?> = _saveHasilError.asStateFlow()
    
    private val _saveHasilSuccess = MutableStateFlow<String?>(null)
    val saveHasilSuccess: StateFlow<String?> = _saveHasilSuccess.asStateFlow()

    private val _pesertaLulusTanpaJadwal = mutableStateListOf<PendaftarResponse>()
    val pesertaLulusTanpaJadwal: List<PendaftarResponse> get() = _pesertaLulusTanpaJadwal
    
    private val _isLoadingPesertaLulus = MutableStateFlow(false)

    private val _pesertaLulusError = MutableStateFlow<String?>(null)

    private val _pesertaLulusCount = MutableStateFlow(0)
    val pesertaLulusCount: StateFlow<Int> = _pesertaLulusCount.asStateFlow()

    private val _isLoadingPesertaLulusCount = MutableStateFlow(false)
    val isLoadingPesertaLulusCount: StateFlow<Boolean> = _isLoadingPesertaLulusCount.asStateFlow()

    private val _pesertaLulusCountError = MutableStateFlow<String?>(null)

    private val _pesertaPendingWawancara = MutableStateFlow<List<PendaftarResponse>>(emptyList())
    val pesertaPendingWawancara: StateFlow<List<PendaftarResponse>> = _pesertaPendingWawancara.asStateFlow()

    private val _isLoadingPesertaPendingWawancara = MutableStateFlow(false)
    val isLoadingPesertaPendingWawancara: StateFlow<Boolean> = _isLoadingPesertaPendingWawancara.asStateFlow()

    private val _pesertaPendingWawancaraError = MutableStateFlow<String?>(null)

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
                    delay(100)
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
            } catch (_: IOException) {
                _jadwalError.value = "Tidak dapat terhubung ke server. Pastikan server berjalan."
                _isLoadingJadwal.value = false
            } catch (e: Exception) {
                _jadwalError.value = "Terjadi kesalahan: ${e.message ?: "Unknown error"}"
                _isLoadingJadwal.value = false
            }
        }
    }
    
    fun loadHasilWawancaraAndUpdateStatus(token: String) {
        viewModelScope.launch {
            loadHasilWawancaraAndUpdateStatusInternal(token)
        }
    }
    
    private suspend fun loadHasilWawancaraAndUpdateStatusInternal(token: String) {
        try {
            val response = hasilWawancaraRepository.getHasilWawancara(token)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.sukses) {
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
        } catch (_: Exception) { }
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
                } catch (_: Exception) { }
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
        } catch (_: Exception) {
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

    fun clearSaveHasilError() { _saveHasilError.value = null }
    fun clearSaveHasilSuccess() { _saveHasilSuccess.value = null }

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

    private fun stopActiveInterview(dayIndex: Int, participantIndex: Int) {
        val key = participantKey(dayIndex, participantIndex)
        activeInterviewJobs.remove(key)?.cancel()
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

    private fun participantKey(dayIndex: Int, participantIndex: Int): String {
        val day = _days.getOrNull(dayIndex)
        val participant = day?.participants?.getOrNull(participantIndex)
        return listOfNotNull(day?.dayName, day?.date, participant?.name, participant?.time)
            .joinToString(separator = "|")
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

}

