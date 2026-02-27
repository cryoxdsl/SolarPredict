package com.example.solarpredict.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.solarpredict.presentation.navigation.Screen

@Composable
fun AppScaffold(
    selected: Screen,
    onNavigate: (Screen) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                Screen.entries.forEach { screen ->
                    NavigationBarItem(
                        selected = selected == screen,
                        onClick = { onNavigate(screen) },
                        label = { Text(screen.label) },
                        icon = {}
                    )
                }
            }
        },
        content = content
    )
}
