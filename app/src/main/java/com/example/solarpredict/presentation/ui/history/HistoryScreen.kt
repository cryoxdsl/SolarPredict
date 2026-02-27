package com.example.solarpredict.presentation.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.solarpredict.presentation.viewmodel.HistoryViewModel
import com.example.solarpredict.presentation.viewmodel.UiState

@Composable
fun HistoryScreen(vm: HistoryViewModel, padding: PaddingValues) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    when (val s = state) {
        UiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(24.dp))
        is UiState.Error -> Text(s.message, modifier = Modifier.padding(24.dp))
        is UiState.Success -> LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card {
                    Text(
                        "MAPE: ${"%.2f".format(s.data.metrics.mapePercent)}% | Biais moyen: ${"%.2f".format(s.data.metrics.meanBiasKwh)} kWh",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            items(s.data.items) { item ->
                Card {
                    Text(
                        "${item.date} - Prévu ${"%.2f".format(item.predictedKwh)} / Réel ${"%.2f".format(item.actualKwh)} kWh",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
