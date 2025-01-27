package com.helgisnw.yangcheonlife.ui.screens

import androidx.compose.foundation.layout.*
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Grade Selection
        OutlinedTextField(
            value = String.format(stringResource(R.string.grade_format), selectedGrade),
            onValueChange = { },
            readOnly = true,
            label = { Text(stringResource(R.string.grade)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Class Selection
        OutlinedTextField(
            value = String.format(stringResource(R.string.classroom_format), selectedClass),
            onValueChange = { },
            readOnly = true,
            label = { Text(stringResource(R.string.classroom)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = {
                updateClassSettings(prefs, selectedGrade, selectedClass, notificationsEnabled)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.done))
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

    val subjects = listOf(
        "없음", "물리", "화학", "생명과학", "지구과학", "윤사", "정치와 법",
        "경제", "세계사", "한국지리", "탐구B", "탐구C", "탐구D"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Subject B Selection
        OutlinedTextField(
            value = selectedSubjectB,
            onValueChange = { },
            readOnly = true,
            label = { Text(stringResource(R.string.subject_b)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subject C Selection
        OutlinedTextField(
            value = selectedSubjectC,
            onValueChange = { },
            readOnly = true,
            label = { Text(stringResource(R.string.subject_c)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subject D Selection
        OutlinedTextField(
            value = selectedSubjectD,
            onValueChange = { },
            readOnly = true,
            label = { Text(stringResource(R.string.subject_d)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = {
                prefs.edit().apply {
                    putString("selectedSubjectB", selectedSubjectB)
                    putString("selectedSubjectC", selectedSubjectC)
                    putString("selectedSubjectD", selectedSubjectD)
                    apply()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.done))
        }
    }
}

private fun updateClassSettings(
    prefs: android.content.SharedPreferences,
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