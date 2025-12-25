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

data class PesertaWawancaraState(
    val pesertaId: Int?,
    val nama: String,
    var status: InterviewStatus = InterviewStatus.PENDING,
    var divisi: String = "",
    var alasan: String = "",
    var durationMinutes: Int = 6,
    var remainingSeconds: Int = 6 * 60,
    var isOngoing: Boolean = false,
    var hasStarted: Boolean = false,
    var hasCompleted: Boolean = false
)

class DetailJadwalWawancaraViewModel : ViewModel() {
    private val hasilWawancaraRepository = HasilWawancaraRepository()

    private val _pesertaStates = mutableMapOf<String, PesertaWawancaraState>()

    private val _timerTick = MutableStateFlow(0L)
    val timerTick: StateFlow<Long> = _timerTick.asStateFlow()

    private val timerJobs = mutableMapOf<String, Job>()

    private val _isSavingHasil = MutableStateFlow(false)
    val isSavingHasil: StateFlow<Boolean> = _isSavingHasil.asStateFlow()
    
    private val _saveHasilError = MutableStateFlow<String?>(null)
    val saveHasilError: StateFlow<String?> = _saveHasilError.asStateFlow()
    
    private val _saveHasilSuccess = MutableStateFlow<String?>(null)
    val saveHasilSuccess: StateFlow<String?> = _saveHasilSuccess.asStateFlow()

    fun initPesertaState(peserta: Peserta) {
        val key = peserta.id?.toString() ?: peserta.nama
        val statusWawancara = peserta.statusWawancara?.lowercase() ?: "pending"
        val initialStatus = when (statusWawancara) {
            "diterima" -> InterviewStatus.ACCEPTED
            "ditolak" -> InterviewStatus.REJECTED
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
            _pesertaStates[key]?.status = initialStatus
        }
    }

    fun getPesertaState(peserta: Peserta): PesertaWawancaraState? {
        val key = peserta.id?.toString() ?: peserta.nama
        return _pesertaStates[key]
    }

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

    fun acceptPeserta(peserta: Peserta, divisi: String, token: String?) {
        val key = peserta.id?.toString() ?: peserta.nama
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

        stopTimer(peserta)

        _isSavingHasil.value = true
        _saveHasilError.value = null
        _saveHasilSuccess.value = null
        
        viewModelScope.launch {
            try {
                val request = HasilWawancaraRequest(
                    pesertaId = peserta.id,
                    status = "diterima",
                    divisi = divisi,
                    alasan = null
                )
                
                val response = hasilWawancaraRepository.simpanHasilWawancara(token, request)
                
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    
                    if (responseBody.sukses && responseBody.data != null) {
                        _pesertaStates[key] = state.copy(
                            status = InterviewStatus.ACCEPTED,
                            divisi = divisi
                        )
                        _saveHasilSuccess.value = "Peserta ${peserta.nama} berhasil diterima dan dimasukkan ke divisi $divisi"
                        _timerTick.value = System.currentTimeMillis()
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

    fun rejectPeserta(peserta: Peserta, token: String?) {
        val key = peserta.id?.toString() ?: peserta.nama
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

        stopTimer(peserta)

        _isSavingHasil.value = true
        _saveHasilError.value = null
        _saveHasilSuccess.value = null
        
        viewModelScope.launch {
            try {
                val request = HasilWawancaraRequest(
                    pesertaId = peserta.id,
                    status = "ditolak",
                    divisi = null,
                    alasan = "Tidak lulus wawancara"
                )
                
                val response = hasilWawancaraRepository.simpanHasilWawancara(token, request)
                
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    
                    if (responseBody.sukses && responseBody.data != null) {
                        _pesertaStates[key] = state.copy(
                            status = InterviewStatus.REJECTED,
                            alasan = "Tidak lulus wawancara"
                        )
                        _saveHasilSuccess.value = "Peserta ${peserta.nama} berhasil ditolak"
                        _timerTick.value = System.currentTimeMillis()
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
        timerJobs.values.forEach { it.cancel() }
        timerJobs.clear()
    }
}
