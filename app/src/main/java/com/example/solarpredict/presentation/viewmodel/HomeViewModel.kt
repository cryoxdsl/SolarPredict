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

data class HomeData(
    val today: DaySummary,
    val tomorrow: DaySummary
)

class HomeViewModel(
    private val container: AppContainer
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<HomeData>>(UiState.Loading)
    val state: StateFlow<UiState<HomeData>> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                val installation = container.configRepository.getInstallation()
                val calibrations = container.configRepository.getCalibrations()
                val start = LocalDate.now()
                val end = start.plusDays(1)
                val weather = container.fetchWeatherHourlyUseCase(start, end, installation.lat, installation.lon)
                weather.forEach { (date, points) ->
                    val forecasts = container.computePvForecastUseCase(date, points, installation, calibrations)
                    container.saveForecastUseCase(forecasts)
                }
                val today = toSummary(container.getForecastUseCase(start))
                val tomorrow = toSummary(container.getForecastUseCase(end))
                HomeData(today = today, tomorrow = tomorrow)
            }.onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "Home load failed") }
        }
    }

    private fun toSummary(forecasts: List<ForecastDay>): DaySummary {
        return DaySummary(
            realistic = forecasts.firstOrNull { it.scenario == Scenario.REALISTIC },
            pessimistic = forecasts.firstOrNull { it.scenario == Scenario.PESSIMISTIC },
            optimistic = forecasts.firstOrNull { it.scenario == Scenario.OPTIMISTIC }
        )
    }
}
