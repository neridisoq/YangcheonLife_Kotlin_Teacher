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
import com.google.firebase.messaging.FirebaseMessaging
import com.helgisnw.yangcheonlifeteacher.R
import com.helgisnw.yangcheonlifeteacher.ui.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialSetupScreen(
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE) }

    var selectedGrade by remember { mutableStateOf(1) }
    var selectedClass by remember { mutableStateOf(1) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    var expandedGrade by remember { mutableStateOf(false) }
    var expandedClass by remember { mutableStateOf(false) }

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
                            text = stringResource(R.string.class_settings),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Grade Selection
                        ExposedDropdownMenuBox(
                            expanded = expandedGrade,
                            onExpandedChange = { expandedGrade = !expandedGrade },
                            modifier = Modifier.fillMaxWidth()
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
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Class Selection
                        ExposedDropdownMenuBox(
                            expanded = expandedClass,
                            onExpandedChange = { expandedClass = !expandedClass },
                            modifier = Modifier.fillMaxWidth()
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
                                        }
                                    )
                                }
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
                            text = "탐구/기초 과목 선택은 설정 메뉴에서 할 수 있습니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Button at the bottom
            Button(
                onClick = {
                    saveSettings(
                        prefs,
                        selectedGrade,
                        selectedClass,
                        notificationsEnabled
                    )
                    onSetupComplete()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(stringResource(R.string.done))
            }
        }
    }
}

private fun saveSettings(
    prefs: SharedPreferences,
    grade: Int,
    classNum: Int,
    notificationsEnabled: Boolean
) {
    prefs.edit().apply {
        putInt("defaultGrade", grade)
        putInt("defaultClass", classNum)
        putBoolean("notificationsEnabled", notificationsEnabled)
        putBoolean("initialSetupCompleted", true)
        apply()
    }

    // FCM 토픽 구독
    if (notificationsEnabled) {
        val topic = "$grade-$classNum"
        android.util.Log.d("FCM_DEBUG", "초기 설정 - FCM 토픽 구독: $topic")
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    android.util.Log.d("FCM_DEBUG", "초기 설정 - FCM 토픽 구독 성공: $topic")
                } else {
                    android.util.Log.e("FCM_DEBUG", "초기 설정 - FCM 토픽 구독 실패: $topic", task.exception)
                }
            }
    }
}