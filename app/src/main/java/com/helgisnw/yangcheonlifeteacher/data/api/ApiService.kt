package com.helgisnw.yangcheonlife.data.api

import com.helgisnw.yangcheonlife.data.model.ScheduleItem
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("{grade}/{classNumber}")
    suspend fun getSchedule(
        @Path("grade") grade: Int,
        @Path("classNumber") classNumber: Int
    ): List<List<ScheduleItem>>
}