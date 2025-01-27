package com.helgisnw.yangcheonlife.ui.screens

import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.firebase.messaging.FirebaseMessaging
import com.helgisnw.yangcheonlife.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassAndGradeSettings() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE) }

    var selectedGrade by remember { mutableStateOf(prefs.getInt("defaultGrade", 1)) }
    var selectedClass by remember { mutableStateOf(prefs.getInt("defaultClass", 1)) }
    var notificationsEnabled by remember { mutableStateOf(prefs.getBoolean("notificationsEnabled", true)) }

    var expandedGrade by remember { mutableStateOf(false) }
    var expandedClass by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
                            updateClassSettings(prefs, grade, selectedClass, notificationsEnabled)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                            updateClassSettings(prefs, selectedGrade, classNum, notificationsEnabled)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            ListItem(
                headlineContent = {
                    Text(stringResource(R.string.changes_saved))
                },
                leadingContent = { Icon(Icons.Filled.CheckCircle, contentDescription = null) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectSelectionSettings() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE) }

    var selectedSubjectB by remember { mutableStateOf(prefs.getString("selectedSubjectB", "없음") ?: "없음") }
    var selectedSubjectC by remember { mutableStateOf(prefs.getString("selectedSubjectC", "없음") ?: "없음") }
    var selectedSubjectD by remember { mutableStateOf(prefs.getString("selectedSubjectD", "없음") ?: "없음") }

    var expandedB by remember { mutableStateOf(false) }
    var expandedC by remember { mutableStateOf(false) }
    var expandedD by remember { mutableStateOf(false) }

    val subjects = listOf(
        "없음", "물리", "화학", "생명과학", "지구과학", "윤사", "정치와 법",
        "경제", "세계사", "한국지리", "탐구B", "탐구C", "탐구D"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expandedB,
            onExpandedChange = { expandedB = !expandedB },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedSubjectB,
                onValueChange = { },
                readOnly = true,
                label = { Text(stringResource(R.string.subject_b)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedB) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expandedB,
                onDismissRequest = { expandedB = false }
            ) {
                subjects.forEach { subject ->
                    DropdownMenuItem(
                        text = { Text(subject) },
                        onClick = {
                            selectedSubjectB = subject
                            expandedB = false
                            prefs.edit().putString("selectedSubjectB", subject).apply()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expandedC,
            onExpandedChange = { expandedC = !expandedC },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedSubjectC,
                onValueChange = { },
                readOnly = true,
                label = { Text(stringResource(R.string.subject_c)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedC) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expandedC,
                onDismissRequest = { expandedC = false }
            ) {
                subjects.forEach { subject ->
                    DropdownMenuItem(
                        text = { Text(subject) },
                        onClick = {
                            selectedSubjectC = subject
                            expandedC = false
                            prefs.edit().putString("selectedSubjectC", subject).apply()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expandedD,
            onExpandedChange = { expandedD = !expandedD },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedSubjectD,
                onValueChange = { },
                readOnly = true,
                label = { Text(stringResource(R.string.subject_d)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedD) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expandedD,
                onDismissRequest = { expandedD = false }
            ) {
                subjects.forEach { subject ->
                    DropdownMenuItem(
                        text = { Text(subject) },
                        onClick = {
                            selectedSubjectD = subject
                            expandedD = false
                            prefs.edit().putString("selectedSubjectD", subject).apply()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            ListItem(
                headlineContent = {
                    Text(stringResource(R.string.changes_saved))
                },
                leadingContent = { Icon(Icons.Filled.CheckCircle, contentDescription = null) }
            )
        }
    }
}

private fun updateClassSettings(
    prefs: SharedPreferences,
    grade: Int,
    classNum: Int,
    notificationsEnabled: Boolean
) {
    val oldGrade = prefs.getInt("defaultGrade", 1)
    val oldClass = prefs.getInt("defaultClass", 1)

    prefs.edit().apply {
        putInt("defaultGrade", grade)
        putInt("defaultClass", classNum)
        apply()
    }

    if (notificationsEnabled) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("$oldGrade-$oldClass")
        FirebaseMessaging.getInstance().subscribeToTopic("$grade-$classNum")
    }
}