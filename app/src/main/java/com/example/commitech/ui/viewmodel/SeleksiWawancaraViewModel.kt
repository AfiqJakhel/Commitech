package com.example.commitech.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

data class DayData(
    val dayName: String,
    val date: String,
    val location: String,
    val participants: List<ParticipantData>
)

data class ParticipantData(
    val time: String,
    val name: String
)

class SeleksiWawancaraViewModel : ViewModel() {
    val days = listOf(
        DayData(
            "Senin", "15 Okt 2025", "Sekretariat BEM KM FTI",
            participants = listOf(
                ParticipantData("07.00 WIB", "Fadhilla Firma"),
                ParticipantData("08.00 WIB", "Afiq Congkel"),
                ParticipantData("09.00 WIB", "Farhan Firki"),
                ParticipantData("10.00 WIB", "Diaz Jelek Hitam")
            )
        ),
        DayData(
            "Selasa", "16 Okt 2025", "Sekretariat BEM KM FTI",
            participants = emptyList()
        ),
        DayData(
            "Rabu", "17 Okt 2025", "Sekretariat BEM KM FTI",
            participants = listOf(
                ParticipantData("07.00 WIB", "Nadya Putri"),
                ParticipantData("08.00 WIB", "Dimas Aditya")
            )
        )
    )
}
