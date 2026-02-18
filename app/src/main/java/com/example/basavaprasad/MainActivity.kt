package com.basavaprasadgola.basavaprasad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.basavaprasadgola.basavaprasad.screens.home.HomeScreen
import com.basavaprasadgola.basavaprasad.screens.textchat.TextChatScreen
import com.basavaprasadgola.basavaprasad.screens.readtext.ReadTextScreen
import com.basavaprasadgola.basavaprasad.screens.voicechat.VoiceChatScreen
import com.basavaprasadgola.basavaprasad.ui.theme.BasavaprasadTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BasavaprasadTheme {
                BasavaprasadApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasavaprasadApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.VOICECHAT) }
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "BASAVAPRASAD",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Logo",
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                )
            }
        ) { innerPadding ->
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreen(Modifier.padding(innerPadding))
                AppDestinations.TEXTCHAT -> TextChatScreen(Modifier.padding(innerPadding))
                AppDestinations.READTEXT -> ReadTextScreen(Modifier.padding(innerPadding))
                AppDestinations.VOICECHAT -> VoiceChatScreen(Modifier.padding(paddingValues = innerPadding))
            }
        }
    }
}