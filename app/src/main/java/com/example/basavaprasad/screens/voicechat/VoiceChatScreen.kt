package com.example.basavaprasad.screens.voicechat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VoiceChatScreen(modifier: Modifier = Modifier.Companion) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "VoiceChat",
            style = MaterialTheme.typography.headlineLarge
        )
    }
}