package com.helgisnw.yangcheonlifeteacher.data.api

import com.helgisnw.yangcheonlifeteacher.data.model.Teacher
import com.helgisnw.yangcheonlifeteacher.data.model.ScheduleItem
import retrofit2.http.GET
import retrofit2.http.Path

interface TeacherApiService {
    @GET("teachers")
    suspend fun getTeachers(): List<Teacher>
    
    @GET("{teacherNumber}")
    suspend fun getTeacherSchedule(
        @Path("teacherNumber") teacherNumber: String
    ): List<List<ScheduleItem>>
}