package com.helgisnw.yangcheonlifeteacher.ui.screens

import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helgisnw.yangcheonlifeteacher.R
import com.helgisnw.yangcheonlifeteacher.ui.components.TopBar
import com.helgisnw.yangcheonlifeteacher.ui.viewmodel.TeacherViewModel
import com.helgisnw.yangcheonlifeteacher.data.model.Teacher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialSetupScreen(
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE) }
    val teacherViewModel: TeacherViewModel = viewModel()

    var selectedTeacher by remember { mutableStateOf<Teacher?>(null) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var expandedTeacher by remember { mutableStateOf(false) }

    val teachers by teacherViewModel.teachers.collectAsState()
    val isLoading by teacherViewModel.isLoading.collectAsState()
    val error by teacherViewModel.error.collectAsState()

    // 에러 처리
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // 에러가 발생하면 스낵바 등으로 표시할 수 있습니다
            teacherViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopBar(title = stringResource(R.string.initial_setup))
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "교사 선택",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            // Teacher Selection
                            ExposedDropdownMenuBox(
                                expanded = expandedTeacher,
                                onExpandedChange = { expandedTeacher = !expandedTeacher },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                value = selectedTeacher?.let { "${it.id} ${it.name}T" } ?: "교사를 선택하세요",
                                onValueChange = { },
                                    readOnly = true,
                                    label = { Text("교사") },
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
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // 새로고침 버튼
                        if (!isLoading) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { teacherViewModel.loadTeachers() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("교사 목록 새로고침")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notifications Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.alert_settings),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 안내 메시지 추가
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "안내",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "교사를 선택하면 해당 교사의 시간표를 확인할 수 있습니다. 추가 설정은 설정 메뉴에서 변경할 수 있습니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Button at the bottom
            Button(
                onClick = {
                    selectedTeacher?.let { teacher ->
                        saveTeacherSettings(
                            prefs,
                            teacher,
                            notificationsEnabled
                        )
                        onSetupComplete()
                    }
                },
                enabled = selectedTeacher != null && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(stringResource(R.string.done))
            }
        }
    }
}

private fun saveTeacherSettings(
    prefs: SharedPreferences,
    teacher: Teacher,
    notificationsEnabled: Boolean
) {
    prefs.edit().apply {
        putString("selectedTeacherId", teacher.id)
        putString("selectedTeacherName", teacher.name)
        putString("selectedTeacherSubject", teacher.subject)
        putBoolean("notificationsEnabled", notificationsEnabled)
        putBoolean("initialSetupCompleted", true)
        apply()
    }
}