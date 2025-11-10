package com.example.commitech.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class ParticipantData(
    val time: String,
    val name: String,
    var status: InterviewStatus = InterviewStatus.PENDING,
    var reason: String = "",
    var division: String = "",
    var durationMinutes: Int = 60,
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

    private val _days = mutableStateListOf<DayData>()
    val days: List<DayData> get() = _days

    private val activeInterviewJobs = mutableMapOf<String, Job>()
    private val scheduledReminderKeys = mutableSetOf<String>()

    private val _events = MutableSharedFlow<InterviewEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<InterviewEvent> = _events.asSharedFlow()

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

    fun rejectWithReason(dayIndex: Int, index: Int, reason: String) {
        cancelInterview(dayIndex, index)
        mutateParticipant(dayIndex, index) { current ->
            current.copy(
                status = InterviewStatus.REJECTED,
                reason = reason
            )
        }
    }

    fun acceptWithDivision(dayIndex: Int, index: Int, division: String) {
        cancelInterview(dayIndex, index)
        mutateParticipant(dayIndex, index) { current ->
            current.copy(
                status = InterviewStatus.ACCEPTED,
                division = division
            )
        }
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
                val warningSecondsBeforeEnd = 5 * 60
                while (remaining > 0) {
                    delay(1_000L)
                    remaining -= 1
                    val shouldWarn = !warned &&
                        participant.durationMinutes >= 10 &&
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

