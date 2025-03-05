package com.helgisnw.yangcheonlife.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScheduleItem(
    val grade: Int,
    val `class`: Int,
    val weekday: Int,
    val weekdayString: String,
    val classTime: Int,
    val teacher: String,
    val subject: String
) : Parcelable