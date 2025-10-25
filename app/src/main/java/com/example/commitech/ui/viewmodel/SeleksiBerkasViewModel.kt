package com.example.commitech.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Peserta(
    val nama: String,
    val lulusBerkas: Boolean,
    val ditolak: Boolean
)

class SeleksiBerkasViewModel : ViewModel() {
    private val _pesertaList = MutableStateFlow<List<Peserta>>(emptyList())
    val pesertaList: StateFlow<List<Peserta>> = _pesertaList

    init {
        loadPeserta()
    }

    private fun loadPeserta() {
        _pesertaList.value = listOf(
            Peserta("Fadhilla Firma", true, false),
            Peserta("Afiq Congkel", false, true),
            Peserta("Farhan Firki", true, false),
            Peserta("Diaz jelek", false, true),
            Peserta("Fadhilla Firma", true, false),
            Peserta("Afiq Congkel", true, false),
            Peserta("Farhan Firki", false, true),
            Peserta("Diaz", false, true)
        )
    }
}
