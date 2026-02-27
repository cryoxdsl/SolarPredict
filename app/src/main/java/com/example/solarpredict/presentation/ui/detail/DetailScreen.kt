package com.example.solarpredict.presentation.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.solarpredict.presentation.ui.components.PowerLineChart
import com.example.solarpredict.presentation.viewmodel.DetailViewModel
import com.example.solarpredict.presentation.viewmodel.UiState

@Composable
fun DetailScreen(vm: DetailViewModel, padding: PaddingValues) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    when (val s = state) {
        UiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(24.dp))
        is UiState.Error -> Text(s.message, modifier = Modifier.padding(24.dp))
        is UiState.Success -> Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Détail ${s.data.date}")
                    Text("Pessimiste")
                    PowerLineChart(s.data.summary.pessimistic?.hourlyPowerW ?: List(24) { 0.0 }, Color(0xFFE53935))
                    Text("Réaliste")
                    PowerLineChart(s.data.summary.realistic?.hourlyPowerW ?: List(24) { 0.0 }, Color(0xFF43A047))
                    Text("Optimiste")
                    PowerLineChart(s.data.summary.optimistic?.hourlyPowerW ?: List(24) { 0.0 }, Color(0xFF1E88E5))
                }
            }
            Card {
                Text("Explications: ${s.data.explanation}", modifier = Modifier.padding(12.dp))
            }
        }
    }
}
