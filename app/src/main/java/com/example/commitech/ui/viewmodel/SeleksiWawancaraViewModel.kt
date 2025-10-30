package com.example.commitech.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel


data class ParticipantData(
    val time: String,
    val name: String,
    var status: InterviewStatus = InterviewStatus.PENDING,
    var reason: String = "",
    var division: String = ""
)

data class DayData(
    val dayName: String,
    val date: String,
    val location: String,
    val participants: List<ParticipantData>
)

class SeleksiWawancaraViewModel : ViewModel() {

    // ✅ Jangan public mutable, gunakan private
    private val _days = mutableStateListOf<DayData>()
    val days: List<DayData> get() = _days

    init {
        _days.addAll(
            listOf(
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
        )
    }

    fun updateParticipant(dayIndex: Int, participantIndex: Int, newDate: String, newTime: String, newLocation: String) {
        val day = _days[dayIndex]
        val updatedList = day.participants.toMutableList()
        val p = updatedList[participantIndex]

        updatedList[participantIndex] = p.copy(time = newTime)
        _days[dayIndex] = day.copy(date = newDate, location = newLocation, participants = updatedList)
    }

    fun totalParticipants(): Int {
        return _days.sumOf { it.participants.size }
    }

    fun getAllParticipants(): List<ParticipantData> {
        return days.flatMap { it.participants }
    }

    fun rejectWithReason(dayIndex: Int, index: Int, reason: String) {
        val day = _days[dayIndex]
        val updated = day.participants.toMutableList()
        val p = updated[index]

        updated[index] = p.copy(
            status = InterviewStatus.REJECTED,
            reason = reason
        )

        _days[dayIndex] = day.copy(participants = updated)
    }

    fun acceptWithDivision(dayIndex: Int, index: Int, division: String) {
        val day = _days[dayIndex]
        val updated = day.participants.toMutableList()
        val p = updated[index]

        updated[index] = p.copy(
            status = InterviewStatus.ACCEPTED,
            division = division
        )

        _days[dayIndex] = day.copy(participants = updated)
    }




}
