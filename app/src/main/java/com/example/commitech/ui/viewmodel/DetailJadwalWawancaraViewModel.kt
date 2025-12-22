package com.example.commitech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.commitech.data.model.HasilWawancaraRequest
import com.example.commitech.data.repository.HasilWawancaraRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data class untuk state peserta dalam detail jadwal wawancara
 */
data class PesertaWawancaraState(
    val pesertaId: Int?,
    val nama: String,
    var status: InterviewStatus = InterviewStatus.PENDING,
    var divisi: String = "",
    var alasan: String = "",
    var durationMinutes: Int = 6, // Durasi timer dalam menit
    var remainingSeconds: Int = 6 * 60, // Sisa waktu dalam detik
    var isOngoing: Boolean = false,
    var hasStarted: Boolean = false,
    var hasCompleted: Boolean = false
)

/**
 * ViewModel untuk mengelola state peserta dalam detail jadwal wawancara
 */
class DetailJadwalWawancaraViewModel : ViewModel() {
    private val hasilWawancaraRepository = HasilWawancaraRepository()
    
    // Map pesertaId atau nama ke state
    private val _pesertaStates = mutableMapOf<String, PesertaWawancaraState>()
    val pesertaStates: Map<String, PesertaWawancaraState> get() = _pesertaStates
    
    // StateFlow untuk trigger recomposition setiap detik saat timer berjalan
    private val _timerTick = MutableStateFlow(0L)
    val timerTick: StateFlow<Long> = _timerTick.asStateFlow()
    
    // Map untuk menyimpan timer jobs
    private val timerJobs = mutableMapOf<String, Job>()
    
    // State untuk API call
    private val _isSavingHasil = MutableStateFlow(false)
    val isSavingHasil: StateFlow<Boolean> = _isSavingHasil.asStateFlow()
    
    private val _saveHasilError = MutableStateFlow<String?>(null)
    val saveHasilError: StateFlow<String?> = _saveHasilError.asStateFlow()
    
    private val _saveHasilSuccess = MutableStateFlow<String?>(null)
    val saveHasilSuccess: StateFlow<String?> = _saveHasilSuccess.asStateFlow()
    
    /**
     * Initialize atau update state peserta
     */
    fun initPesertaState(peserta: Peserta) {
        val key = peserta.id?.toString() ?: peserta.nama
        val statusWawancara = peserta.statusWawancara?.lowercase() ?: "pending"
        val initialStatus = when {
            statusWawancara == "diterima" -> InterviewStatus.ACCEPTED
            statusWawancara == "ditolak" -> InterviewStatus.REJECTED
            else -> InterviewStatus.PENDING
        }
        
        if (!_pesertaStates.containsKey(key)) {
            _pesertaStates[key] = PesertaWawancaraState(
                pesertaId = peserta.id,
                nama = peserta.nama,
                status = initialStatus,
                durationMinutes = 6,
                remainingSeconds = 6 * 60
            )
        } else {
            // Update status jika sudah ada state
            _pesertaStates[key]?.status = initialStatus
        }
    }
    
    /**
     * Get state peserta
     */
    fun getPesertaState(peserta: Peserta): PesertaWawancaraState? {
        val key = peserta.id?.toString() ?: peserta.nama
        return _pesertaStates[key]
    }
    
    /**
     * Set durasi timer untuk peserta
     */
    fun setDuration(peserta: Peserta, minutes: Int) {
        val key = peserta.id?.toString() ?: peserta.nama
        // Auto-initialize jika state belum ada
        if (!_pesertaStates.containsKey(key)) {
            initPesertaState(peserta)
        }
        val state = _pesertaStates[key] ?: return
        _pesertaStates[key] = state.copy(
            durationMinutes = minutes,
            remainingSeconds = minutes * 60
        )
        // Trigger recomposition untuk update UI
        _timerTick.value = System.currentTimeMillis()
    }
    
    /**
     * Start timer untuk peserta
     */
    fun startTimer(peserta: Peserta) {
        val key = peserta.id?.toString() ?: peserta.nama
        // Auto-initialize jika state belum ada
        if (!_pesertaStates.containsKey(key)) {
            initPesertaState(peserta)
        }
        val state = _pesertaStates[key] ?: return
        
        // Cancel existing timer jika ada
        timerJobs[key]?.cancel()
        
        // Pastikan remainingSeconds sudah di-set dari durationMinutes jika masih default
        val finalRemainingSeconds = if (state.remainingSeconds <= 0 && state.durationMinutes > 0) {
            state.durationMinutes * 60
        } else {
            state.remainingSeconds
        }
        
        // Update state dengan remainingSeconds yang benar
        _pesertaStates[key] = state.copy(
            isOngoing = true,
            hasStarted = true,
            hasCompleted = false,
            remainingSeconds = finalRemainingSeconds
        )
        
        // Trigger recomposition immediately untuk menampilkan timer
        _timerTick.value = System.currentTimeMillis()
        
        // Start timer job dengan remainingSeconds yang sudah di-update
        val job = viewModelScope.launch {
            var remaining = finalRemainingSeconds
            while (remaining > 0 && _pesertaStates[key]?.isOngoing == true) {
                delay(1000)
                remaining--
                val currentState = _pesertaStates[key]
                if (currentState != null) {
                    _pesertaStates[key] = currentState.copy(remainingSeconds = remaining)
                    // Trigger recomposition setiap detik
                    _timerTick.value = System.currentTimeMillis()
                }
                
                if (remaining == 0) {
                    // Timer selesai
                    val finalState = _pesertaStates[key]
                    if (finalState != null) {
                        _pesertaStates[key] = finalState.copy(
                            isOngoing = false,
                            hasCompleted = true
                        )
                        _timerTick.value = System.currentTimeMillis()
                    }
                }
            }
        }
        
        timerJobs[key] = job
    }
    
    /**
     * Pause timer untuk peserta (bisa dilanjutkan lagi)
     */
    fun pauseTimer(peserta: Peserta) {
        val key = peserta.id?.toString() ?: peserta.nama
        timerJobs[key]?.cancel()
        timerJobs.remove(key)
        
        val state = _pesertaStates[key] ?: return
        _pesertaStates[key] = state.copy(
            isOngoing = false
        )
        _timerTick.value = System.currentTimeMillis()
    }
    
    /**
     * Resume timer untuk peserta (lanjutkan dari sisa waktu sebelumnya)
     */
    fun resumeTimer(peserta: Peserta) {
        val key = peserta.id?.toString() ?: peserta.nama
        val state = _pesertaStates[key] ?: return
        
        // Cancel existing timer jika ada
        timerJobs[key]?.cancel()
        
        // Update state
        _pesertaStates[key] = state.copy(
            isOngoing = true,
            hasStarted = true
        )
        
        // Start timer job dengan sisa waktu yang ada
        val job = viewModelScope.launch {
            var remaining = state.remainingSeconds
            while (remaining > 0 && _pesertaStates[key]?.isOngoing == true) {
                delay(1000)
                remaining--
                val currentState = _pesertaStates[key]
                if (currentState != null) {
                    _pesertaStates[key] = currentState.copy(remainingSeconds = remaining)
                    // Trigger recomposition setiap detik
                    _timerTick.value = System.currentTimeMillis()
                }
                
                if (remaining == 0) {
                    // Timer selesai
                    val finalState = _pesertaStates[key]
                    if (finalState != null) {
                        _pesertaStates[key] = finalState.copy(
                            isOngoing = false,
                            hasCompleted = true
                        )
                        _timerTick.value = System.currentTimeMillis()
                    }
                }
            }
        }
        
        timerJobs[key] = job
    }
    
    /**
     * Stop timer untuk peserta (selesai, tidak bisa dilanjutkan)
     */
    fun stopTimer(peserta: Peserta) {
        val key = peserta.id?.toString() ?: peserta.nama
        timerJobs[key]?.cancel()
        timerJobs.remove(key)
        
        val state = _pesertaStates[key] ?: return
        _pesertaStates[key] = state.copy(
            isOngoing = false,
            hasCompleted = true
        )
        _timerTick.value = System.currentTimeMillis()
    }
    
    /**
     * Format waktu tersisa menjadi string MM:SS
     */
    fun formatRemainingTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }
    
    /**
     * Terima peserta dengan divisi
     */
    fun acceptPeserta(peserta: Peserta, divisi: String, token: String?) {
        val key = peserta.id?.toString() ?: peserta.nama
        // Auto-initialize jika state belum ada
        if (!_pesertaStates.containsKey(key)) {
            initPesertaState(peserta)
        }
        val state = _pesertaStates[key] ?: return
        
        if (peserta.id == null) {
            _saveHasilError.value = "Data peserta tidak valid. Peserta ID tidak tersedia."
            return
        }
        
        if (token == null) {
            _saveHasilError.value = "Token tidak tersedia. Silakan login ulang."
            return
        }
        
        if (divisi.isBlank()) {
            _saveHasilError.value = "Divisi harus dipilih untuk peserta yang diterima."
            return
        }
        
        // Stop timer
        stopTimer(peserta)
        
        // Set loading state
        _isSavingHasil.value = true
        _saveHasilError.value = null
        _saveHasilSuccess.value = null
        
        viewModelScope.launch {
            try {
                val request = HasilWawancaraRequest(
                    pesertaId = peserta.id!!,
                    status = "diterima",
                    divisi = divisi,
                    alasan = null
                )
                
                val response = hasilWawancaraRepository.simpanHasilWawancara(token, request)
                
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    
                    if (responseBody.sukses && responseBody.data != null) {
                        // Success: Update local state
                        _pesertaStates[key] = state.copy(
                            status = InterviewStatus.ACCEPTED,
                            divisi = divisi
                        )
                        _saveHasilSuccess.value = "Peserta ${peserta.nama} berhasil diterima dan dimasukkan ke divisi $divisi"
                        // Trigger timer tick untuk update UI (refresh card)
                        _timerTick.value = System.currentTimeMillis()
                        // Trigger recomposition dengan update state lagi
                        delay(100)
                        _timerTick.value = System.currentTimeMillis()
                    } else {
                        _saveHasilError.value = responseBody.pesan ?: "Gagal menyimpan hasil wawancara"
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _saveHasilError.value = "Gagal menyimpan hasil wawancara: ${response.code()} - $errorBody"
                }
            } catch (e: Exception) {
                _saveHasilError.value = "Error: ${e.message}"
            } finally {
                _isSavingHasil.value = false
            }
        }
    }
    
    /**
     * Tolak peserta
     */
    fun rejectPeserta(peserta: Peserta, token: String?) {
        val key = peserta.id?.toString() ?: peserta.nama
        // Auto-initialize jika state belum ada
        if (!_pesertaStates.containsKey(key)) {
            initPesertaState(peserta)
        }
        val state = _pesertaStates[key] ?: return
        
        if (peserta.id == null) {
            _saveHasilError.value = "Data peserta tidak valid. Peserta ID tidak tersedia."
            return
        }
        
        if (token == null) {
            _saveHasilError.value = "Token tidak tersedia. Silakan login ulang."
            return
        }
        
        // Stop timer
        stopTimer(peserta)
        
        // Set loading state
        _isSavingHasil.value = true
        _saveHasilError.value = null
        _saveHasilSuccess.value = null
        
        viewModelScope.launch {
            try {
                val request = HasilWawancaraRequest(
                    pesertaId = peserta.id!!,
                    status = "ditolak",
                    divisi = null,
                    alasan = "Tidak lulus wawancara"
                )
                
                val response = hasilWawancaraRepository.simpanHasilWawancara(token, request)
                
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    
                    if (responseBody.sukses && responseBody.data != null) {
                        // Success: Update local state
                        _pesertaStates[key] = state.copy(
                            status = InterviewStatus.REJECTED,
                            alasan = "Tidak lulus wawancara"
                        )
                        _saveHasilSuccess.value = "Peserta ${peserta.nama} berhasil ditolak"
                        // Trigger timer tick untuk update UI (refresh card)
                        _timerTick.value = System.currentTimeMillis()
                        // Trigger recomposition dengan update state lagi
                        delay(100)
                        _timerTick.value = System.currentTimeMillis()
                    } else {
                        _saveHasilError.value = responseBody.pesan ?: "Gagal menyimpan hasil wawancara"
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _saveHasilError.value = "Gagal menyimpan hasil wawancara: ${response.code()} - $errorBody"
                }
            } catch (e: Exception) {
                _saveHasilError.value = "Error: ${e.message}"
            } finally {
                _isSavingHasil.value = false
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Cancel all timer jobs
        timerJobs.values.forEach { it.cancel() }
        timerJobs.clear()
    }
}
