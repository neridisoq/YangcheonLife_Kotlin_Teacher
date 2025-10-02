package com.helgisnw.yangcheonlifeteacher.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Teacher(
    val id: String,
    val name: String,
    val subject: String? = null,
    val grade: Int? = null,
    val classNumber: Int? = null
) : Parcelable