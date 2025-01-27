package com.helgisnw.yangcheonlife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.helgisnw.yangcheonlife.ui.screens.InitialSetupScreen
import com.helgisnw.yangcheonlife.ui.screens.MainScreen
import com.helgisnw.yangcheonlife.ui.theme.YangcheonLifeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YangcheonLifeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent()
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    val context = LocalContext.current
    var showInitialSetup by remember {
        mutableStateOf(
            !context.getSharedPreferences("app_settings", ComponentActivity.MODE_PRIVATE)
                .getBoolean("initialSetupCompleted", false)
        )
    }

    if (showInitialSetup) {
        InitialSetupScreen(
            onSetupComplete = {
                context.getSharedPreferences("app_settings", ComponentActivity.MODE_PRIVATE)
                    .edit()
                    .putBoolean("initialSetupCompleted", true)
                    .apply()
                showInitialSetup = false
            }
        )
    } else {
        MainScreen()
    }
}