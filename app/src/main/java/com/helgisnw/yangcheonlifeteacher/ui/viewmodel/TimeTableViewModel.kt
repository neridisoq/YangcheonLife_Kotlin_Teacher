package com.helgisnw.yangcheonlifeteacher.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helgisnw.yangcheonlifeteacher.data.model.ScheduleItem
import com.helgisnw.yangcheonlifeteacher.data.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import java.text.SimpleDateFormat
import java.util.Locale

class TimeTableViewModel : ViewModel() {
    private val repository = ScheduleRepository()
    private val _scheduleState = MutableStateFlow<List<List<ScheduleItem>>>(emptyList())
    val scheduleState: StateFlow<List<List<ScheduleItem>>> = _scheduleState

    private val periodTimes = listOf(
        "08:20" to "09:10",
        "09:20" to "10:10",
        "10:20" to "11:10",
        "11:20" to "12:10",
        "13:10" to "14:00",
        "14:10" to "15:00",
        "15:10" to "16:00"
    )

    fun loadSchedule(grade: Int, classNumber: Int) {
        viewModelScope.launch {
            repository.getSchedule(grade, classNumber)
                .onSuccess { schedule ->
                    _scheduleState.value = schedule
                }
                .onFailure {
                    // Handle error
                }
        }
    }

    fun loadTeacherSchedule(teacherNumber: String) {
        viewModelScope.launch {
            repository.getTeacherSchedule(teacherNumber)
                .onSuccess { schedule ->
                    _scheduleState.value = schedule
                }
                .onFailure {
                    // Handle error
                }
        }
    }

    fun isCurrentPeriod(period: Int, dayOfWeek: Int): Boolean {
        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Calendar.MONDAY is 2, but our dayOfWeek parameter starts from 1
        // So we need to add 1 to match the Calendar system
        if (currentDayOfWeek != dayOfWeek + 1 || currentDayOfWeek < Calendar.MONDAY || currentDayOfWeek > Calendar.FRIDAY) {
            return false
        }

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = calendar.time
        val (startTimeStr, endTimeStr) = periodTimes[period - 1]

        try {
            val periodStart = timeFormat.parse(startTimeStr)
            val periodEnd = timeFormat.parse(endTimeStr)

            // Set the dates to the same day for proper comparison
            val currentTimeCalendar = Calendar.getInstance().apply {
                time = currentTime
            }

            val startCalendar = Calendar.getInstance().apply {
                time = periodStart!!
                set(Calendar.YEAR, currentTimeCalendar.get(Calendar.YEAR))
                set(Calendar.MONTH, currentTimeCalendar.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, currentTimeCalendar.get(Calendar.DAY_OF_MONTH))
            }

            val endCalendar = Calendar.getInstance().apply {
                time = periodEnd!!
                set(Calendar.YEAR, currentTimeCalendar.get(Calendar.YEAR))
                set(Calendar.MONTH, currentTimeCalendar.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, currentTimeCalendar.get(Calendar.DAY_OF_MONTH))
            }

            return currentTime.time >= startCalendar.timeInMillis &&
                    currentTime.time <= endCalendar.timeInMillis
        } catch (e: Exception) {
            return false
        }
    }

    fun getDayOfWeek(index: Int): String {
        return when (index) {
            0 -> "월"
            1 -> "화"
            2 -> "수"
            3 -> "목"
            4 -> "금"
            else -> ""
        }
    }

    fun getPeriodTime(period: Int): Pair<String, String> {
        return periodTimes.getOrNull(period - 1) ?: ("" to "")
    }
}