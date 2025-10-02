package com.helgisnw.yangcheonlifeteacher.data.service

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.helgisnw.yangcheonlifeteacher.data.model.SpecialRoomWiFi
import com.helgisnw.yangcheonlifeteacher.data.model.WiFiConnectionResult
import com.helgisnw.yangcheonlifeteacher.data.model.WiFiConnectionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class WiFiService(private val context: Context) {
    
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    
    /**
     * WiFi 연결 전 상태 확인 및 초기화
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun prepareForConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1초간 지연하여 시스템이 네트워크 상태를 정리할 시간을 줍니다.
            delay(1000)
            
            // WiFi 상태 확인
            if (!wifiManager.isWifiEnabled) {
                return@withContext false
            }
            
            return@withContext true
        } catch (e: Exception) {
            return@withContext false
        }
    }
    
    /**
     * 일반 교실 WiFi 연결
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun connectToClassroom(grade: Int, classNumber: Int): WiFiConnectionResult = withContext(Dispatchers.IO) {
        // WiFi 연결 전 준비
        if (!prepareForConnection()) {
            return@withContext WiFiConnectionResult(
                isSuccess = false,
                message = "WiFi가 비활성화되어 있습니다. WiFi를 켜주세요.",
                connectionType = WiFiConnectionType.RegularClassroom(grade, classNumber)
            )
        }
        
        val connectionType = WiFiConnectionType.RegularClassroom(grade, classNumber)
        return@withContext connectToWiFi(connectionType)
    }
    
    /**
     * 특별실 WiFi 연결
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun connectToSpecialRoom(room: SpecialRoomWiFi): WiFiConnectionResult = withContext(Dispatchers.IO) {
        // WiFi 연결 전 준비
        if (!prepareForConnection()) {
            return@withContext WiFiConnectionResult(
                isSuccess = false,
                message = "WiFi가 비활성화되어 있습니다. WiFi를 켜주세요.",
                connectionType = WiFiConnectionType.SpecialRoom(room)
            )
        }
        
        val connectionType = WiFiConnectionType.SpecialRoom(room)
        return@withContext connectToWiFi(connectionType)
    }
    
    /**
     * WiFi 연결 실행
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectToWiFi(connectionType: WiFiConnectionType): WiFiConnectionResult {
        return try {
            // 기존 제안들을 정리 (선택사항)
            clearExistingSuggestions()
            
            val suggestion = WifiNetworkSuggestion.Builder()
                .setSsid(connectionType.ssid)
                .setWpa2Passphrase(connectionType.password)
                .setIsAppInteractionRequired(true) // 사용자 승인 필요
                .setIsHiddenSsid(true) // 숨겨진 SSID 설정
                .build()

            val suggestions = listOf(suggestion)
            val status = wifiManager.addNetworkSuggestions(suggestions)

            when (status) {
                WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS -> {
                    WiFiConnectionResult(
                        isSuccess = true,
                        message = "WiFi 추천이 완료되었습니다!\n\n📱 'WiFi 설정 열기' 버튼을 눌러 WiFi 설정에서 '양천고라이프 앱을 통해 추천됨' 항목을 찾아 연결해주세요.\n\n🔍 연결할 네트워크: ${connectionType.ssid}",
                        connectionType = connectionType
                    )
                }
                WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_DUPLICATE -> {
                    WiFiConnectionResult(
                        isSuccess = true,
                        message = "이미 추천된 네트워크입니다.\n\n📱 'WiFi 설정 열기' 버튼을 눌러 WiFi 설정에서 '양천고라이프 앱을 통해 추천됨' 또는 '${connectionType.ssid}' 네트워크를 찾아 연결해주세요.",
                        connectionType = connectionType
                    )
                }
                WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_APP_DISALLOWED -> {
                    WiFiConnectionResult(
                        isSuccess = false,
                        message = "앱의 WiFi 권한이 제한되어 있습니다.\n\n📱 'WiFi 설정 열기' 버튼을 누르고, 설정 > 앱 > 양천고라이프 > 권한에서 WiFi 권한을 허용해주세요.\n또는 WiFi 설정에서 '${connectionType.ssid}'를 수동으로 연결해주세요.",
                        connectionType = connectionType
                    )
                }
                WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_INTERNAL -> {
                    WiFiConnectionResult(
                        isSuccess = false,
                        message = "내부 오류가 발생했습니다.\n다시 시도해주세요.",
                        connectionType = connectionType
                    )
                }
                WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_EXCEEDS_MAX_PER_APP -> {
                    WiFiConnectionResult(
                        isSuccess = false,
                        message = "최대 네트워크 추천 수를 초과했습니다.",
                        connectionType = connectionType
                    )
                }
                else -> {
                    // 모든 실패 케이스에 대해 사용자에게 수동 연결 안내
                    WiFiConnectionResult(
                        isSuccess = false,
                        message = "WiFi 추천에 실패했습니다.\n\n📱 'WiFi 설정 열기' 버튼을 눌러 WiFi 설정에서 '${connectionType.ssid}'를 직접 연결해주세요.\n\n🔑 비밀번호: ${connectionType.password}",
                        connectionType = connectionType
                    )
                }
            }
        } catch (e: Exception) {
            // 자동 연결 실패시 수동 연결 안내
            WiFiConnectionResult(
                isSuccess = false,
                message = "WiFi 연결 중 오류가 발생했습니다.\n\n📱 'WiFi 설정 열기' 버튼을 눌러 WiFi 설정에서 '${connectionType.ssid}'를 직접 연결해주세요.\n\n🔑 비밀번호: ${connectionType.password}",
                connectionType = connectionType
            )
        }
    }
    
    /**
     * 기존 네트워크 제안들을 정리
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun clearExistingSuggestions() {
        try {
            val currentSuggestions = wifiManager.networkSuggestions
            if (currentSuggestions.isNotEmpty()) {
                wifiManager.removeNetworkSuggestions(currentSuggestions)
            }
        } catch (e: Exception) {
            // 정리 실패는 무시하고 계속 진행
        }
    }
    
    /**
     * WiFi 설정 화면 열기
     */
    private fun openWiFiSettings() {
        try {
            val intent = Intent(Settings.Panel.ACTION_WIFI)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // 설정 화면 열기 실패시 일반 WiFi 설정으로 대체
            try {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e2: Exception) {
                // 무시
            }
        }
    }
    
    /**
     * API 레벨 체크
     */
    fun isWiFiSuggestionSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }
}