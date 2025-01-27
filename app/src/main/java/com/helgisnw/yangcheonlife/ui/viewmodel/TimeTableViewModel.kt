package com.helgisnw.yangcheonlife.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helgisnw.yangcheonlife.data.model.ScheduleItem
import com.helgisnw.yangcheonlife.data.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

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

    fun isCurrentPeriod(period: Int, dayOfWeek: Int): Boolean {
        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY

        if (currentDayOfWeek != dayOfWeek - 1 || currentDayOfWeek < 0 || currentDayOfWeek > 4) {
            return false
        }

        val currentTime = calendar.time
        val timeFormat = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
        val (startTime, endTime) = getPeriodTime(period)

        return try {
            val periodStart = timeFormat.parse(startTime)
            val periodEnd = timeFormat.parse(endTime)
            currentTime in periodStart..periodEnd
        } catch (e: Exception) {
            false
        }
    }
}