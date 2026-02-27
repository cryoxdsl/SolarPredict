package com.example.solarpredict.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solarpredict.AppContainer
import com.example.solarpredict.domain.model.HistoryItem
import com.example.solarpredict.domain.model.Metrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HistoryData(
    val items: List<HistoryItem>,
    val metrics: Metrics,
    val monthFilter: Int
)

class HistoryViewModel(
    private val container: AppContainer
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<HistoryData>>(UiState.Loading)
    val state: StateFlow<UiState<HistoryData>> = _state.asStateFlow()

    fun load(month: Int = LocalDate.now().monthValue) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                val year = LocalDate.now().year
                val start = LocalDate.of(year, month, 1)
                val end = start.withDayOfMonth(start.lengthOfMonth())
                val history = container.getHistoryUseCase(start, end)
                val metrics = container.computeMetricsUseCase(history)
                HistoryData(history, metrics, month)
            }.onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "History load failed") }
        }
    }
}
