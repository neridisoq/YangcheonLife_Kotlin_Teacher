package com.helgisnw.yangcheonlife.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helgisnw.yangcheonlife.R
import com.helgisnw.yangcheonlife.data.model.ScheduleItem
import com.helgisnw.yangcheonlife.ui.viewmodel.TimeTableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTableScreen(
    viewModel: TimeTableViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }

    var selectedGrade by remember { mutableStateOf(prefs.getInt("defaultGrade", 1)) }
    var selectedClass by remember { mutableStateOf(prefs.getInt("defaultClass", 1)) }
    var selectedSubjectB by remember { mutableStateOf(prefs.getString("selectedSubjectB", "탐구B") ?: "탐구B") }
    var selectedSubjectC by remember { mutableStateOf(prefs.getString("selectedSubjectC", "탐구C") ?: "탐구C") }
    var selectedSubjectD by remember { mutableStateOf(prefs.getString("selectedSubjectD", "탐구D") ?: "탐구D") }

    var expandedGrade by remember { mutableStateOf(false) }
    var expandedClass by remember { mutableStateOf(false) }

    val scheduleState by viewModel.scheduleState.collectAsState()
    val cellBackgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)

    LaunchedEffect(selectedGrade, selectedClass) {
        viewModel.loadSchedule(selectedGrade, selectedClass)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExposedDropdownMenuBox(
                expanded = expandedGrade,
                onExpandedChange = { expandedGrade = !expandedGrade },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = String.format(stringResource(R.string.grade_format), selectedGrade),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text(stringResource(R.string.grade)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGrade) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedGrade,
                    onDismissRequest = { expandedGrade = false }
                ) {
                    (1..3).forEach { grade ->
                        DropdownMenuItem(
                            text = { Text(String.format(stringResource(R.string.grade_format), grade)) },
                            onClick = {
                                selectedGrade = grade
                                expandedGrade = false
                                prefs.edit().putInt("defaultGrade", grade).apply()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            ExposedDropdownMenuBox(
                expanded = expandedClass,
                onExpandedChange = { expandedClass = !expandedClass },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = String.format(stringResource(R.string.classroom_format), selectedClass),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text(stringResource(R.string.classroom)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClass) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedClass,
                    onDismissRequest = { expandedClass = false }
                ) {
                    (1..11).forEach { classNum ->
                        DropdownMenuItem(
                            text = { Text(String.format(stringResource(R.string.classroom_format), classNum)) },
                            onClick = {
                                selectedClass = classNum
                                expandedClass = false
                                prefs.edit().putInt("defaultClass", classNum).apply()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { viewModel.loadSchedule(selectedGrade, selectedClass) }) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header row
            items(6) { col ->
                HeaderCell(col)
            }

            // Time slots and schedule
            repeat(7) { row ->
                item {
                    TimeSlotCell(row + 1)
                }

                items(5) { col ->
                    val scheduleItem = scheduleState.getOrNull(col)?.getOrNull(row)
                    ScheduleCell(
                        scheduleItem = scheduleItem,
                        isCurrentPeriod = viewModel.isCurrentPeriod(row + 1, col + 1),
                        backgroundColor = cellBackgroundColor,
                        selectedSubjectB = selectedSubjectB,
                        selectedSubjectC = selectedSubjectC,
                        selectedSubjectD = selectedSubjectD
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(col: Int) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
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
                .padding(4.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TimeSlotCell(period: Int) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
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
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = stringResource(getTimePeriodResource(period)),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ScheduleCell(
    scheduleItem: ScheduleItem?,
    isCurrentPeriod: Boolean,
    backgroundColor: Color,
    selectedSubjectB: String,
    selectedSubjectC: String,
    selectedSubjectD: String
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(BorderStroke(1.dp, Color.Gray))
            .background(if (isCurrentPeriod) backgroundColor else Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val displaySubject = when (scheduleItem?.subject) {
                "탐구B" -> selectedSubjectB
                "탐구C" -> selectedSubjectC
                "탐구D" -> selectedSubjectD
                else -> scheduleItem?.subject
            }

            Text(
                text = displaySubject ?: "",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            if (scheduleItem?.teacher?.isNotBlank() == true) {
                Text(
                    text = scheduleItem.teacher,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun getTimePeriodResource(period: Int): Int {
    return when (period) {
        1 -> R.string.time_period_1
        2 -> R.string.time_period_2
        3 -> R.string.time_period_3
        4 -> R.string.time_period_4
        5 -> R.string.time_period_5
        6 -> R.string.time_period_6
        7 -> R.string.time_period_7
        else -> R.string.time_period_1
    }
}