package com.helgisnw.yangcheonlifeteacher.data.repository

import com.helgisnw.yangcheonlifeteacher.data.api.RetrofitClient
import com.helgisnw.yangcheonlifeteacher.data.model.ScheduleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScheduleRepository {
    suspend fun getSchedule(grade: Int, classNumber: Int): Result<List<List<ScheduleItem>>> =
        withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getSchedule(grade, classNumber)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}