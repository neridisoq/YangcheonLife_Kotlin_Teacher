package com.helgisnw.yangcheonlife.data.model

data class ScheduleItem(
    val grade: Int,
    val `class`: Int,
    val weekday: Int,
    val weekdayString: String,
    val classTime: Int,
    val teacher: String,
    val subject: String
)