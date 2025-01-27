package com.helgisnw.yangcheonlife.ui.screens

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
            Scaffold(
                topBar = {
                    TopBar(
                        title = stringResource(R.string.subject_selection),
                        showBackButton = true,
                        onBackClick = { navController.navigateUp() }
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    SubjectSelectionSettings()
                }
            }
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
                    headlineContent = { Text(stringResource(R.string.subject_selection)) },
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