package com.helgisnw.yangcheonlifeteacher.data.repository

import com.helgisnw.yangcheonlifeteacher.data.api.TeacherRetrofitClient
import com.helgisnw.yangcheonlifeteacher.data.model.Teacher
import com.helgisnw.yangcheonlifeteacher.data.model.ScheduleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TeacherRepository {
    private val apiService = TeacherRetrofitClient.apiService

    suspend fun getTeachers(): Result<List<Teacher>> = withContext(Dispatchers.IO) {
        try {
            val teachers = apiService.getTeachers()
            Result.success(teachers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTeacherSchedule(teacherNumber: String): Result<List<List<ScheduleItem>>> = withContext(Dispatchers.IO) {
        try {
            val schedule = apiService.getTeacherSchedule(teacherNumber)
            Result.success(schedule)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}