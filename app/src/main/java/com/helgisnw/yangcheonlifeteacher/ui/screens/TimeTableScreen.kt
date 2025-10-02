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
import com.helgisnw.yangcheonlifeteacher.ui.components.TopBar
import com.helgisnw.yangcheonlifeteacher.ui.viewmodel.TimeTableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTableScreen(
    viewModel: TimeTableViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }

    // 교사 정보를 SharedPreferences에서 가져오기
    var selectedTeacherId by remember { mutableStateOf(prefs.getString("selectedTeacherId", "") ?: "") }
    var selectedTeacherName by remember { mutableStateOf(prefs.getString("selectedTeacherName", "") ?: "") }

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

    // Time table total height calculation (top bar height + teacher info area height + bottom navigation)
    val topBarHeight = 56.dp
    val teacherInfoAreaHeight = 80.dp
    val bottomNavHeight = 80.dp // Approximate height of bottom navigation bar
    val totalVerticalPadding = 16.dp // Reduced padding
    val availableHeight = screenHeight - topBarHeight - teacherInfoAreaHeight - bottomNavHeight - totalVerticalPadding

    // Cell size calculation
    val cellSize = minOf(
        (screenWidth.value / 6).dp,
        (availableHeight.value / 8).dp
    )

    LaunchedEffect(selectedTeacherId) {
        if (selectedTeacherId.isNotEmpty()) {
            viewModel.loadTeacherSchedule(selectedTeacherId)
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
            // 교사 정보 표시 영역
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
                    Column(modifier = Modifier.weight(1f)) {
                        if (selectedTeacherName.isNotEmpty()) {
                            Text(
                                text = "담당 교사: $selectedTeacherName",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "교사 번호: $selectedTeacherId",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "교사 정보를 불러올 수 없습니다.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    IconButton(onClick = {
                        if (selectedTeacherId.isNotEmpty()) {
                            viewModel.loadTeacherSchedule(selectedTeacherId)
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