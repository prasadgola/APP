package com.example.basavaprasad.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Hi I am basavaprasad Gola",
            style = MaterialTheme.typography.headlineLarge
        )
    }
}