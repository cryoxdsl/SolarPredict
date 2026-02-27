package com.example.solarpredict.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solarpredict.AppContainer
import com.example.solarpredict.domain.model.DaySummary
import com.example.solarpredict.domain.model.ForecastDay
import com.example.solarpredict.domain.model.Scenario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DetailData(
    val date: LocalDate,
    val summary: DaySummary,
    val explanation: String
)

class DetailViewModel(
    private val container: AppContainer
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<DetailData>>(UiState.Loading)
    val state: StateFlow<UiState<DetailData>> = _state.asStateFlow()

    fun load(date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                val forecasts = container.getForecastUseCase(date)
                val summary = DaySummary(
                    realistic = forecasts.firstOrNull { it.scenario == Scenario.REALISTIC },
                    pessimistic = forecasts.firstOrNull { it.scenario == Scenario.PESSIMISTIC },
                    optimistic = forecasts.firstOrNull { it.scenario == Scenario.OPTIMISTIC }
                )
                DetailData(
                    date = date,
                    summary = summary,
                    explanation = buildExplanation(summary.realistic)
                )
            }.onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "Detail load failed") }
        }
    }

    private fun buildExplanation(realistic: ForecastDay?): String {
        if (realistic == null) return "Aucune donnée disponible"
        return "PR et pertes appliqués, clipping=${if (realistic.clippingLikely) "probable" else "faible"}, pic à ${realistic.peakHour}h"
    }
}
