package com.helgisnw.yangcheonlife.ui.screens

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.helgisnw.yangcheonlife.R
import com.helgisnw.yangcheonlife.ui.components.TopBar

@Composable
fun SettingsScreen() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "settings_main") {
        composable("settings_main") {
            SettingsMainContent(navController)
        }
        composable("class_settings") {
            Scaffold(
                topBar = {
                    TopBar(
                        title = stringResource(R.string.class_settings),
                        showBackButton = true,
                        onBackClick = { navController.navigateUp() }
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    ClassAndGradeSettings()
                }
            }
        }
        composable("subject_settings") {
            SubjectSelectionScreen(mainNavController = navController)
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

    Scaffold(
        topBar = {
            TopBar(title = stringResource(R.string.settings))
        }
    ) { paddingValues ->
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
                    headlineContent = { Text(stringResource(R.string.class_settings)) },
                    leadingContent = { Icon(Icons.Default.School, contentDescription = null) }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { navController.navigate("subject_settings") }
            ) {
                ListItem(
                    headlineContent = { Text("탐구/기초 과목 선택") },
                    leadingContent = { Icon(Icons.Default.Book, contentDescription = null) }
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

                                if (enabled) {
                                    subscribeToCurrentTopic(prefs)
                                } else {
                                    unsubscribeFromCurrentTopic(prefs)
                                }
                            }
                        )
                    }
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

private fun openWebPage(context: android.content.Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

private fun subscribeToCurrentTopic(prefs: SharedPreferences) {
    val grade = prefs.getInt("defaultGrade", 1)
    val classNum = prefs.getInt("defaultClass", 1)
    val topic = "$grade-$classNum"
    FirebaseMessaging.getInstance().subscribeToTopic(topic)
}

private fun unsubscribeFromCurrentTopic(prefs: SharedPreferences) {
    val grade = prefs.getInt("defaultGrade", 1)
    val classNum = prefs.getInt("defaultClass", 1)
    val topic = "$grade-$classNum"
    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
}