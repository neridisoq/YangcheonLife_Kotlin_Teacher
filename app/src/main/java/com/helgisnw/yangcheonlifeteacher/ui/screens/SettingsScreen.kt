package com.helgisnw.yangcheonlifeteacher.ui.screens

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Divider
import androidx.compose.runtime.rememberCoroutineScope
import com.helgisnw.yangcheonlifeteacher.data.model.SpecialRoomsData
import com.helgisnw.yangcheonlifeteacher.data.model.WiFiConnectionResult
import com.helgisnw.yangcheonlifeteacher.data.service.WiFiService
import com.helgisnw.yangcheonlifeteacher.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun SettingsScreen() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "settings_main") {
        composable("settings_main") {
            SettingsMainContent(navController)
        }
        composable("class_settings") {
            Scaffold { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    TeacherSettings()
                }
            }
        }
        composable("advanced_settings") {
            Scaffold { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    AdvancedSettingsContent()
                }
            }
        }
        composable("wifi_settings") {
            Scaffold { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    WiFiConnectionContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsMainContent(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE) }

    var notificationsEnabled by remember {
        mutableStateOf(prefs.getBoolean("notificationsEnabled", true))
    }

    var showColorPicker by remember { mutableStateOf(false) }
    val defaultColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f).toArgb()
    var selectedColor by remember {
        mutableStateOf(
            Color(prefs.getInt("cellBackgroundColor", defaultColor))
        )
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = selectedColor,
            onColorSelected = { color ->
                selectedColor = color
                prefs.edit().putInt("cellBackgroundColor", color.toArgb()).apply()
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { navController.navigate("class_settings") }
            ) {
                ListItem(
                    headlineContent = { Text("선생님 설정") },
                    leadingContent = { Icon(Icons.Default.School, contentDescription = null) }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.privacy_policy)) },
                        leadingContent = { Icon(Icons.Default.Policy, contentDescription = null) },
                        modifier = Modifier.clickable {
                            openWebPage(context, "https://yangcheon.sen.hs.kr/dggb/module/policy/selectPolicyDetail.do?policyTypeCode=PLC002&menuNo=75574")
                        }
                    )

                    ListItem(
                        headlineContent = { Text(stringResource(R.string.goto_school_web)) },
                        leadingContent = { Icon(Icons.Default.Web, contentDescription = null) },
                        modifier = Modifier.clickable {
                            openWebPage(context, "https://yangcheon.sen.hs.kr")
                        }
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.color_picker)) },
                    leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                    trailingContent = {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(selectedColor)
                                .clickable { showColorPicker = true }
                        )
                    }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.alert_settings)) },
                    leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    trailingContent = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { enabled ->
                                notificationsEnabled = enabled
                                prefs.edit().putBoolean("notificationsEnabled", enabled).apply()
                            }
                        )
                    }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { navController.navigate("wifi_settings") }
            ) {
                ListItem(
                    headlineContent = { Text("WiFi 연결") },
                    leadingContent = { Icon(Icons.Default.Wifi, contentDescription = null) }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { navController.navigate("advanced_settings") }
            ) {
                ListItem(
                    headlineContent = { Text("고급 설정") },
                    leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:neridisoq@icloud.com")
                        }
                        context.startActivity(intent)
                    }
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.support)) },
                    leadingContent = { Icon(Icons.Default.Email, contentDescription = null) }
                )
            }
        }
    }
}

@Composable
private fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(
        Color(0xFFBBDEFB), // Light Blue
        Color(0xFFB3E5FC), // Lighter Blue
        Color(0xFFC8E6C9), // Light Green
        Color(0xFFFFF9C4), // Light Yellow
        Color(0xFFFFCCBC), // Light Red
        Color(0xFFE1BEE7), // Light Purple
        Color(0xFFF8BBD0), // Light Pink
        Color(0xFFFFE0B2), // Light Orange
        Color(0xFFD7CCC8), // Light Brown
        Color(0xFFF5F5F5)  // Light Gray
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.color_picker)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.color_picker_description),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.padding(8.dp)
                ) {
                    items(colors.size) { index ->
                        ColorItem(
                            color = colors[index],
                            isSelected = colors[index] == initialColor,
                            onClick = { onColorSelected(colors[index]) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

@Composable
private fun ColorItem(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier
                }
            )
    )
}

@Composable
private fun WiFiConnectionContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val wifiService = remember { WiFiService(context) }
    
    var selectedTab by remember { mutableStateOf(2) } // 기본값: 3학년
    var connectionResult by remember { mutableStateOf<WiFiConnectionResult?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var isConnecting by remember { mutableStateOf(false) }

    val tabTitles = listOf("1학년", "2학년", "3학년", "특별실")
    
    if (showResult) {
        AlertDialog(
            onDismissRequest = { showResult = false },
            title = { Text("WiFi 연결 결과") },
            text = { 
                connectionResult?.let { result ->
                    Text(result.message)
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showResult = false
                    // WiFi 설정 화면 열기
                    try {
                        val intent = android.content.Intent(android.provider.Settings.Panel.ACTION_WIFI)
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        try {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e2: Exception) {
                            // 무시
                        }
                    }
                }) {
                    Text("WiFi 설정 열기")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResult = false }) {
                    Text("닫기")
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 탭 선택
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // 컨텐츠
        when (selectedTab) {
            0 -> GradeClassroomContent(1, wifiService, isConnecting) { result ->
                connectionResult = result
                showResult = true
                isConnecting = false
            }
            1 -> GradeClassroomContent(2, wifiService, isConnecting) { result ->
                connectionResult = result
                showResult = true
                isConnecting = false
            }
            2 -> GradeClassroomContent(3, wifiService, isConnecting) { result ->
                connectionResult = result
                showResult = true
                isConnecting = false
            }
            3 -> SpecialRoomContent(wifiService, isConnecting) { result ->
                connectionResult = result
                showResult = true
                isConnecting = false
            }
        }
    }
}

@Composable
private fun GradeClassroomContent(
    grade: Int,
    wifiService: WiFiService,
    isConnecting: Boolean,
    onResult: (WiFiConnectionResult) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "교실별 WiFi 연결 도우미",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• 연결하기 버튼을 누르면 WiFi 설정 화면이 열립니다.\n" +
                              "• '양천고라이프 앱을 통해 추천됨' 항목을 찾아 연결하세요.\n" +
                              "• 또는 해당 교실의 SSID를 직접 검색하여 연결하세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        items((1..11).toList()) { classNumber ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${grade}학년 ${classNumber}반",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "SSID: ${grade}-${classNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Button(
                        onClick = {
                            if (wifiService.isWiFiSuggestionSupported()) {
                                scope.launch {
                                    val result = wifiService.connectToClassroom(grade, classNumber)
                                    onResult(result)
                                }
                            } else {
                                onResult(
                                    WiFiConnectionResult(
                                        isSuccess = false,
                                        message = "이 기능은 안드로이드 10 (API 29) 이상에서만 지원됩니다.",
                                        connectionType = com.helgisnw.yangcheonlifeteacher.data.model.WiFiConnectionType.RegularClassroom(grade, classNumber)
                                    )
                                )
                            }
                        },
                        enabled = !isConnecting
                    ) {
                        if (isConnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("연결")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecialRoomContent(
    wifiService: WiFiService,
    isConnecting: Boolean,
    onResult: (WiFiConnectionResult) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val specialRooms = SpecialRoomsData.allRooms
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "특별실 WiFi 연결 도우미",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• 연결하기 버튼을 누르면 WiFi 설정 화면이 열립니다.\n" +
                              "• '양천고라이프 앱을 통해 추천됨' 항목 또는 해당 특별실 SSID를 찾아 연결하세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        items(specialRooms) { room ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = room.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "SSID: ${room.ssid}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Button(
                        onClick = {
                            if (wifiService.isWiFiSuggestionSupported()) {
                                scope.launch {
                                    val result = wifiService.connectToSpecialRoom(room)
                                    onResult(result)
                                }
                            } else {
                                onResult(
                                    WiFiConnectionResult(
                                        isSuccess = false,
                                        message = "이 기능은 안드로이드 10 (API 29) 이상에서만 지원됩니다.",
                                        connectionType = com.helgisnw.yangcheonlifeteacher.data.model.WiFiConnectionType.SpecialRoom(room)
                                    )
                                )
                            }
                        },
                        enabled = !isConnecting
                    ) {
                        if (isConnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("연결")
                        }
                    }
                }
            }
        }
    }
}

private fun openWebPage(context: android.content.Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

@Composable
private fun AdvancedSettingsContent() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE) }
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("설정 초기화") },
            text = { Text("모든 설정이 초기화됩니다. 계속하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 모든 설정 초기화
                        prefs.edit().clear().apply()
                        showResetDialog = false
                        
                        // 앱 재시작을 위해 MainActivity를 다시 시작
                        val intent = Intent(context, context::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        (context as? android.app.Activity)?.finish()
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column {
                ListItem(
                    headlineContent = { Text("설정 초기화") },
                    supportingContent = { Text("모든 앱 설정을 초기 상태로 되돌립니다.") },
                    leadingContent = { Icon(Icons.Default.RestartAlt, contentDescription = null) },
                    modifier = Modifier.clickable { showResetDialog = true }
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "참고사항",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "설정을 초기화하면 앱이 자동으로 재시작되며, 초기 설정 화면이 다시 표시됩니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}