package com.example.solarpredict.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.solarpredict.domain.model.DaySummary
import com.example.solarpredict.presentation.ui.components.PowerLineChart
import com.example.solarpredict.presentation.viewmodel.HomeViewModel
import com.example.solarpredict.presentation.viewmodel.UiState

@Composable
fun HomeScreen(vm: HomeViewModel, padding: PaddingValues) {
    val state by vm.state.collectAsState()
    when (val s = state) {
        UiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(24.dp))
        is UiState.Error -> Text(s.message, modifier = Modifier.padding(24.dp))
        is UiState.Success -> Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DayCard("Aujourd'hui", s.data.today)
            DayCard("Demain", s.data.tomorrow)
            Button(onClick = { vm.refresh() }) { Text("Rafraîchir") }
        }
    }
}

@Composable
private fun DayCard(title: String, summary: DaySummary) {
    val realistic = summary.realistic
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text("kWh: ${"%.2f".format(realistic?.dailyKwh ?: 0.0)}")
            Text(
                "Intervalle: ${"%.2f".format(summary.pessimistic?.dailyKwh ?: 0.0)} / ${"%.2f".format(realistic?.dailyKwh ?: 0.0)} / ${"%.2f".format(summary.optimistic?.dailyKwh ?: 0.0)}"
            )
            Text("Pic: ${realistic?.peakHour ?: 0}h - ${"%.0f".format(realistic?.peakW ?: 0.0)}W")
            PowerLineChart(values = realistic?.hourlyPowerW ?: List(24) { 0.0 }, color = Color(0xFF1E88E5))
        }
    }
}
