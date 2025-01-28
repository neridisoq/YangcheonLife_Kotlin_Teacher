package com.helgisnw.yangcheonlife

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.helgisnw.yangcheonlife.ui.screens.InitialSetupScreen
import com.helgisnw.yangcheonlife.ui.screens.MainScreen
import com.helgisnw.yangcheonlife.ui.theme.YangcheonLifeTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 13 이상에서 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        checkAndRequestPermissions()

        setContent {
            YangcheonLifeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent()
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.FOREGROUND_SERVICE
        )

        val permissionsToRequest = mutableListOf<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                showNotificationPermissionRationale()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun showNotificationPermissionRationale() {
        MaterialAlertDialogBuilder(this)
            .setTitle("알림 권한 필요")
            .setMessage("시간표 변경 및 학교 공지사항을 받기 위해 알림 권한이 필요합니다.")
            .setPositiveButton("권한 설정") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 알림 권한이 승인됨
                } else {
                    // 알림 권한이 거부됨
                    showPermissionDeniedDialog()
                }
            }
            PERMISSIONS_REQUEST_CODE -> {
                // 다른 권한들의 승인/거부 처리
                handlePermissionsResult(permissions, grantResults)
            }
        }
    }

    private fun handlePermissionsResult(
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val deniedPermissions = mutableListOf<String>()

        for (i in permissions.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permissions[i])
            }
        }

        if (deniedPermissions.isNotEmpty()) {
            showPermissionDeniedDialog()
        }
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("권한 거부됨")
            .setMessage("앱의 원활한 사용을 위해 필요한 권한들이 거부되었습니다. 설정에서 권한을 허용해주세요.")
            .setPositiveButton("설정으로 이동") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun openAppSettings() {
        val intent = android.content.Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            android.net.Uri.fromParts("package", packageName, null)
        )
        startActivity(intent)
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1
        private const val PERMISSIONS_REQUEST_CODE = 2
    }
}

@Composable
fun MainContent() {
    val context = LocalContext.current
    var showInitialSetup by remember {
        mutableStateOf(
            !context.getSharedPreferences("app_settings", ComponentActivity.MODE_PRIVATE)
                .getBoolean("initialSetupCompleted", false)
        )
    }

    if (showInitialSetup) {
        InitialSetupScreen(
            onSetupComplete = {
                context.getSharedPreferences("app_settings", ComponentActivity.MODE_PRIVATE)
                    .edit()
                    .putBoolean("initialSetupCompleted", true)
                    .apply()
                showInitialSetup = false
            }
        )
    } else {
        MainScreen()
    }
}