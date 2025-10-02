package com.helgisnw.yangcheonlifeteacher.ui.screens

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.launch
import com.helgisnw.yangcheonlifeteacher.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helgisnw.yangcheonlifeteacher.data.model.ScheduleItem
import com.helgisnw.yangcheonlifeteacher.data.model.Teacher
import com.helgisnw.yangcheonlifeteacher.data.model.WiFiConnectionResult
import com.helgisnw.yangcheonlifeteacher.data.service.WiFiService
import com.helgisnw.yangcheonlifeteacher.ui.viewmodel.TimeTableViewModel
import com.helgisnw.yangcheonlifeteacher.ui.viewmodel.TeacherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTableScreen(
    viewModel: TimeTableViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
    val teacherViewModel: TeacherViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val wifiService = remember { WiFiService(context) }
    
    // WiFi 연결 관련 상태
    var showWifiResult by remember { mutableStateOf(false) }
    var wifiConnectionResult by remember { mutableStateOf<WiFiConnectionResult?>(null) }
    var isConnectingWifi by remember { mutableStateOf(false) }

    // 교사 정보를 SharedPreferences에서 가져오기
    var selectedTeacher by remember { 
        mutableStateOf<Teacher?>(
            run {
                val savedId = prefs.getString("selectedTeacherId", "")
                val savedName = prefs.getString("selectedTeacherName", "")
                if (!savedId.isNullOrEmpty() && !savedName.isNullOrEmpty()) {
                    Teacher(
                        id = savedId,
                        name = savedName,
                        subject = prefs.getString("selectedTeacherSubject", "")
                    )
                } else null
            }
        )
    }
    var expandedTeacher by remember { mutableStateOf(false) }

    val teachers by teacherViewModel.teachers.collectAsState()
    val isLoadingTeachers by teacherViewModel.isLoading.collectAsState()

    val defaultColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f).toArgb()
    var cellBackgroundColor by remember {
        mutableStateOf(
            Color(prefs.getInt("cellBackgroundColor", defaultColor))
        )
    }

    val scheduleState by viewModel.scheduleState.collectAsState()

    LaunchedEffect(selectedTeacher?.id) {
        selectedTeacher?.id?.let { teacherId ->
            viewModel.loadTeacherSchedule(teacherId)
        }
    }

    // WiFi 연결 결과 다이얼로그
    if (showWifiResult) {
        AlertDialog(
            onDismissRequest = { showWifiResult = false },
            title = { Text("WiFi 연결 결과") },
            text = { 
                wifiConnectionResult?.let { result ->
                    Text(result.message)
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showWifiResult = false
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
                TextButton(onClick = { showWifiResult = false }) {
                    Text("닫기")
                }
            }
        )
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 교사 선택 영역
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoadingTeachers) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        // Teacher Selection Dropdown
                        ExposedDropdownMenuBox(
                            expanded = expandedTeacher,
                            onExpandedChange = { expandedTeacher = !expandedTeacher },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedTeacher?.let { "${it.id} ${it.name}T" } ?: "선생님을 선택하세요",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("선생님") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTeacher) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedTeacher,
                                onDismissRequest = { expandedTeacher = false }
                            ) {
                                teachers.forEach { teacher ->
                                    DropdownMenuItem(
                                        text = { 
                                            Column {
                                                Text("${teacher.id} ${teacher.name}T")
                                                teacher.subject?.let { subject ->
                                                    Text(
                                                        text = subject,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedTeacher = teacher
                                            expandedTeacher = false
                                            
                                            // SharedPreferences에 저장
                                            prefs.edit().apply {
                                                putString("selectedTeacherId", teacher.id)
                                                putString("selectedTeacherName", teacher.name)
                                                putString("selectedTeacherSubject", teacher.subject)
                                                apply()
                                            }
                                            
                                            // 시간표 로드
                                            viewModel.loadTeacherSchedule(teacher.id)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = {
                        // 교사 목록 새로고침
                        teacherViewModel.loadTeachers()
                        
                        // 선택된 교사가 있으면 시간표도 새로고침
                        selectedTeacher?.id?.let { teacherId ->
                            viewModel.loadTeacherSchedule(teacherId)
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 시간표 - 사용 가능한 영역 내에서 1:1 비율 유지
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val widthBasedCell = maxWidth / 6
                val heightBasedCell = if (maxHeight == Dp.Unspecified || !maxHeight.value.isFinite()) {
                    widthBasedCell
                } else {
                    maxHeight / 8
                }
                val cellSize = minOf(widthBasedCell, heightBasedCell).coerceAtLeast(0.dp)
                val tableWidth = cellSize * 6
                val tableHeight = cellSize * 8

                Column(
                    modifier = Modifier
                        .width(tableWidth)
                        .height(tableHeight)
                ) {
                    // Header row
                    Row {
                        repeat(6) { col ->
                            HeaderCell(col, cellSize)
                        }
                    }

                    // Time slots and schedule rows
                    repeat(7) { row ->
                        Row {
                            // Time slot cell
                            TimeSlotCell(row + 1, cellSize)
                            
                            // Schedule cells for each day
                            repeat(5) { col ->
                                val scheduleItem = scheduleState.getOrNull(col)?.getOrNull(row)
                                ScheduleCell(
                                    scheduleItem = scheduleItem,
                                    isCurrentPeriod = viewModel.isCurrentPeriod(row + 1, col + 1),
                                    backgroundColor = cellBackgroundColor,
                                    prefs = prefs,
                                    cellSize = cellSize,
                                    onWifiConnect = { grade, classNumber ->
                                        if (wifiService.isWiFiSuggestionSupported()) {
                                            isConnectingWifi = true
                                            scope.launch {
                                                val result = wifiService.connectToClassroom(grade, classNumber)
                                                wifiConnectionResult = result
                                                showWifiResult = true
                                                isConnectingWifi = false
                                            }
                                        } else {
                                            wifiConnectionResult = WiFiConnectionResult(
                                                isSuccess = false,
                                                message = "이 기능은 안드로이드 10 (API 29) 이상에서만 지원됩니다.",
                                                connectionType = com.helgisnw.yangcheonlifeteacher.data.model.WiFiConnectionType.RegularClassroom(grade, classNumber)
                                            )
                                            showWifiResult = true
                                        }
                                    },
                                    isConnectingWifi = isConnectingWifi
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(col: Int, cellSize: Dp) {
    Box(
        modifier = Modifier
            .size(cellSize)
            .border(BorderStroke(1.dp, Color.Gray))
            .background(Color.Gray.copy(alpha = 0.3f))
    ) {
        Text(
            text = when (col) {
                0 -> ""
                1 -> stringResource(R.string.mon)
                2 -> stringResource(R.string.tue)
                3 -> stringResource(R.string.wed)
                4 -> stringResource(R.string.thu)
                else -> stringResource(R.string.fri)
            },
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight(Alignment.CenterVertically),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TimeSlotCell(period: Int, cellSize: Dp) {
    Box(
        modifier = Modifier
            .size(cellSize)
            .border(BorderStroke(1.dp, Color.Gray))
            .background(Color.Gray.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = String.format(stringResource(R.string.period_format), period),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = getStartTime(period),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ScheduleCell(
    scheduleItem: ScheduleItem?,
    isCurrentPeriod: Boolean,
    backgroundColor: Color,
    prefs: SharedPreferences,
    cellSize: Dp,
    onWifiConnect: (Int, Int) -> Unit,
    isConnectingWifi: Boolean
) {
    // 교실 정보에서 학년반 파싱하는 함수
    fun parseClassInfo(locationText: String): Pair<Int, Int>? {
        // "105", "205", "301" 등의 형태에서 학년과 반 추출
        if (locationText.length == 3 && locationText.all { it.isDigit() }) {
            val grade = locationText[0].toString().toIntOrNull()
            val classNumber = locationText.substring(1).toIntOrNull()
            if (grade != null && classNumber != null && grade in 1..3 && classNumber in 1..11) {
                return Pair(grade, classNumber)
            }
        }
        return null
    }

    Box(
        modifier = Modifier
            .size(cellSize)
            .border(BorderStroke(1.dp, Color.Gray))
            .background(if (isCurrentPeriod) backgroundColor else Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (scheduleItem?.subject != null) {
                var displaySubject = scheduleItem.subject
                var displayLocation = scheduleItem.teacher

                // A반과 같은 형식인 경우 사용자 설정 확인
                if (scheduleItem.subject.contains("반")) {
                    val selectedSubject = prefs.getString("selected${scheduleItem.subject}Subject", null)
                    if (!selectedSubject.isNullOrEmpty() && selectedSubject != "선택 없음" && selectedSubject != scheduleItem.subject) {
                        val components = selectedSubject.split("/")
                        if (components.size == 2) {
                            displaySubject = components[0]
                            displayLocation = components[1]
                        }
                    }
                }

                // 과목명 (더 큰 글씨)
                Text(
                    text = displaySubject,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                // 교실/선생님 정보와 WiFi 아이콘을 포함하는 Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // 교실/선생님 정보 (작은 글씨)
                    Text(
                        text = displayLocation,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, false)
                    )
                    
                    // 교실 정보가 파싱 가능한 경우 WiFi 아이콘 표시
                    parseClassInfo(displayLocation)?.let { (grade, classNumber) ->
                        Spacer(modifier = Modifier.width(2.dp))
                        
                        if (isConnectingWifi) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "WiFi 연결",
                                modifier = Modifier
                                    .size(12.dp)
                                    .clickable {
                                        onWifiConnect(grade, classNumber)
                                    },
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getStartTime(period: Int): String {
    return when (period) {
        1 -> "08:20"
        2 -> "09:20"
        3 -> "10:20"
        4 -> "11:20"
        5 -> "13:10"
        6 -> "14:10"
        7 -> "15:10"
        else -> ""
    }
}