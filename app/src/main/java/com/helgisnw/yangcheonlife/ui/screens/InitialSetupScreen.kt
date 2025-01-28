package com.helgisnw.yangcheonlife.ui.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.messaging.FirebaseMessaging
import com.helgisnw.yangcheonlife.R
import com.helgisnw.yangcheonlife.ui.components.TopBar

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
    var selectedSubjectB by remember { mutableStateOf("없음") }
    var selectedSubjectC by remember { mutableStateOf("없음") }
    var selectedSubjectD by remember { mutableStateOf("없음") }

    var expandedGrade by remember { mutableStateOf(false) }
    var expandedClass by remember { mutableStateOf(false) }
    var expandedSubjectB by remember { mutableStateOf(false) }
    var expandedSubjectC by remember { mutableStateOf(false) }
    var expandedSubjectD by remember { mutableStateOf(false) }

    val subjects = listOf(
        "없음", "물리", "화학", "생명과학", "지구과학", "윤사", "정치와 법",
        "경제", "세계사", "한국지리", "탐구B", "탐구C", "탐구D"
    )

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

                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.subject_selection),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Subject Selections
                        SubjectDropdown(
                            selectedSubject = selectedSubjectB,
                            onSubjectSelected = { selectedSubjectB = it },
                            expanded = expandedSubjectB,
                            onExpandedChange = { expandedSubjectB = it },
                            subjects = subjects,
                            label = stringResource(R.string.subject_b)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SubjectDropdown(
                            selectedSubject = selectedSubjectC,
                            onSubjectSelected = { selectedSubjectC = it },
                            expanded = expandedSubjectC,
                            onExpandedChange = { expandedSubjectC = it },
                            subjects = subjects,
                            label = stringResource(R.string.subject_c)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SubjectDropdown(
                            selectedSubject = selectedSubjectD,
                            onSubjectSelected = { selectedSubjectD = it },
                            expanded = expandedSubjectD,
                            onExpandedChange = { expandedSubjectD = it },
                            subjects = subjects,
                            label = stringResource(R.string.subject_d)
                        )
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
            }

            // Button at the bottom
            Button(
                onClick = {
                    saveSettings(
                        prefs,
                        selectedGrade,
                        selectedClass,
                        notificationsEnabled,
                        selectedSubjectB,
                        selectedSubjectC,
                        selectedSubjectD
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectDropdown(
    selectedSubject: String,
    onSubjectSelected: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    subjects: List<String>,
    label: String
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange(it) },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedSubject,
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            subjects.forEach { subject ->
                DropdownMenuItem(
                    text = { Text(subject) },
                    onClick = {
                        onSubjectSelected(subject)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

private fun saveSettings(
    prefs: SharedPreferences,
    grade: Int,
    classNum: Int,
    notificationsEnabled: Boolean,
    subjectB: String,
    subjectC: String,
    subjectD: String
) {
    prefs.edit().apply {
        putInt("defaultGrade", grade)
        putInt("defaultClass", classNum)
        putBoolean("notificationsEnabled", notificationsEnabled)
        putString("selectedSubjectB", subjectB)
        putString("selectedSubjectC", subjectC)
        putString("selectedSubjectD", subjectD)
        putBoolean("initialSetupCompleted", true)
        apply()
    }

    if (notificationsEnabled) {
        FirebaseMessaging.getInstance().subscribeToTopic("$grade-$classNum")
    }
}