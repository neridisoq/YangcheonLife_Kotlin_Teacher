package com.helgisnw.yangcheonlifeteacher.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 특별실 WiFi 정보 모델
 */
@Parcelize
data class SpecialRoomWiFi(
    val name: String,        // 특별실 이름
    val ssid: String,        // WiFi SSID
    val password: String     // WiFi 비밀번호
) : Parcelable

/**
 * WiFi 연결 유형 sealed class
 */
sealed class WiFiConnectionType {
    data class RegularClassroom(val grade: Int, val classNumber: Int) : WiFiConnectionType()
    data class SpecialRoom(val room: SpecialRoomWiFi) : WiFiConnectionType()
    
    val displayName: String
        get() = when (this) {
            is RegularClassroom -> "${grade}학년 ${classNumber}반 교실"
            is SpecialRoom -> room.name
        }
    
    val ssid: String
        get() = when (this) {
            is RegularClassroom -> "$grade-$classNumber"
            is SpecialRoom -> room.ssid
        }
    
    val password: String
        get() = when (this) {
            is RegularClassroom -> {
                val suffix = String.format("%d%02d", grade, classNumber)
                "yangcheon$suffix"
            }
            is SpecialRoom -> room.password
        }
}

/**
 * WiFi 연결 결과 모델
 */
data class WiFiConnectionResult(
    val isSuccess: Boolean,     // 연결 성공 여부
    val message: String,        // 결과 메시지
    val connectionType: WiFiConnectionType // 연결 유형
)

/**
 * 특별실 WiFi 데이터
 */
object SpecialRoomsData {
    
    /**
     * 모든 특별실 WiFi 정보
     */
    val allRooms: List<SpecialRoomWiFi> = listOf(
        SpecialRoomWiFi("화학생명실", "화학생명실", "yangcheon401"),
        SpecialRoomWiFi("홈베이스A/B", "홈베이스", "yangcheon402"),
        SpecialRoomWiFi("음악실", "음악실", "yangcheon403"),
        SpecialRoomWiFi("소강당", "소강당", "yangcheon404"),
        SpecialRoomWiFi("미술실", "미술실", "yangcheon405"),
        SpecialRoomWiFi("물리지학실", "물리지학실", "yangcheon406"),
        SpecialRoomWiFi("멀티스튜디오", "멀티스튜디오", "yangcheon407"),
        SpecialRoomWiFi("다목적실A/B", "다목적실AB", "yangcheon408"),
        SpecialRoomWiFi("꿈담카페A", "꿈담카페A", "yangcheon409"),
        SpecialRoomWiFi("꿈담카페B", "꿈담카페B", "yangcheon410"),
        SpecialRoomWiFi("도서실", "도서실", "yangcheon411"),
        SpecialRoomWiFi("세미나실", "세미나실", "yangcheon412"),
        SpecialRoomWiFi("상록실", "상록실", "yangcheon413"),
        SpecialRoomWiFi("senWiFi_Free", "senWiFi_Free", "888884444g")
    )
    
    /**
     * 특별실 이름으로 WiFi 정보 찾기
     */
    fun findRoom(name: String): SpecialRoomWiFi? {
        // 정확한 이름으로 먼저 찾기
        allRooms.find { it.name == name }?.let { return it }
        
        // 부분 일치로 찾기 (홈베이스A, 다목적실B 등)
        return allRooms.find { room ->
            name.contains(room.name.split("/")[0]) || room.name.contains(name)
        }
    }
    
    /**
     * 교실명으로 특별실인지 확인
     */
    fun isSpecialRoom(classroom: String): Boolean {
        val trimmedName = classroom.trim()
        
        // 일반 교실 번호 형식인지 확인 (3자리 숫자)
        if (trimmedName.length == 3 && trimmedName.toIntOrNull() != null) {
            return false
        }
        
        // 'T'가 포함된 경우 일반 교실
        if (trimmedName.contains("T")) {
            return false
        }
        
        // 특별실 목록에 있는지 확인
        return findRoom(trimmedName) != null
    }
}