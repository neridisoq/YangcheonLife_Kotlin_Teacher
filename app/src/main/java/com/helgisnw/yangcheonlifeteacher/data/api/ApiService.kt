package com.helgisnw.yangcheonlifeteacher.data.api

import com.helgisnw.yangcheonlifeteacher.data.model.ScheduleItem
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("{grade}/{classNumber}")
    suspend fun getSchedule(
        @Path("grade") grade: Int,
        @Path("classNumber") classNumber: Int
    ): List<List<ScheduleItem>>
}