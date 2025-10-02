package com.helgisnw.yangcheonlifeteacher.ui.screens

import android.content.Context
import android.content.SharedPreferences
import com.helgisnw.yangcheonlifeteacher.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helgisnw.yangcheonlifeteacher.data.model.ScheduleItem
import com.helgisnw.yangcheonlifeteacher.data.model.Teacher
import com.helgisnw.yangcheonlifeteacher.ui.components.TopBar
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

    // Screen size calculation
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp - 32.dp // 좌우 패딩 16dp씩 제외
    val screenHeight = configuration.screenHeightDp.dp

    // Time table total height calculation (top bar height + teacher selection area height + bottom navigation)
    val topBarHeight = 56.dp
    val teacherSelectionAreaHeight = 80.dp
    val bottomNavHeight = 80.dp // Approximate height of bottom navigation bar
    val totalVerticalPadding = 16.dp // Reduced padding
    val availableHeight = screenHeight - topBarHeight - teacherSelectionAreaHeight - bottomNavHeight - totalVerticalPadding

    // Cell size calculation
    val cellSize = minOf(
        (screenWidth.value / 6).dp,
        (availableHeight.value / 8).dp
    )

    LaunchedEffect(selectedTeacher?.id) {
        selectedTeacher?.id?.let { teacherId ->
            viewModel.loadTeacherSchedule(teacherId)
        }
    }

    Scaffold(
        topBar = {
            TopBar(title = stringResource(R.string.timetable))
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 교사 선택 영역
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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

            Spacer(modifier = Modifier.height(16.dp))

            // 시간표 그리드를 가운데 정렬
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.Center)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier
                        .width(cellSize * 6)
                        .height(cellSize * 8)
                ) {
                    // Header row
                    items(6) { col ->
                        HeaderCell(col, cellSize)
                    }

                    // Time slots and schedule
                    repeat(7) { row ->
                        item {
                            TimeSlotCell(row + 1, cellSize)
                        }

                        items(5) { col ->
                            val scheduleItem = scheduleState.getOrNull(col)?.getOrNull(row)
                            ScheduleCell(
                                scheduleItem = scheduleItem,
                                isCurrentPeriod = viewModel.isCurrentPeriod(row + 1, col + 1),
                                backgroundColor = cellBackgroundColor,
                                prefs = prefs,
                                cellSize = cellSize
                            )
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
    cellSize: Dp
) {
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

                // 교실/선생님 정보 (작은 글씨)
                Text(
                    text = displayLocation,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
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