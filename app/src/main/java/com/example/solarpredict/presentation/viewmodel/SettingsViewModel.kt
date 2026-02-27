package com.example.solarpredict.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solarpredict.AppContainer
import com.example.solarpredict.domain.model.AppConfig
import com.example.solarpredict.domain.model.Installation
import com.example.solarpredict.domain.model.ShadingLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsData(
    val installation: Installation,
    val config: AppConfig,
    val message: String? = null
)

class SettingsViewModel(
    private val container: AppContainer
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<SettingsData>>(UiState.Loading)
    val state: StateFlow<UiState<SettingsData>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                SettingsData(
                    installation = container.configRepository.getInstallation(),
                    config = container.configRepository.getAppConfig()
                )
            }.onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "Settings load failed") }
        }
    }

    fun saveInstallation(
        lat: String,
        lon: String,
        kwp: String,
        azimuth: String,
        tilt: String,
        losses: String,
        shading: ShadingLevel,
        pacMax: String
    ) {
        viewModelScope.launch {
            val current = (_state.value as? UiState.Success)?.data ?: return@launch
            val model = current.installation.copy(
                lat = lat.toDoubleOrNull() ?: current.installation.lat,
                lon = lon.toDoubleOrNull() ?: current.installation.lon,
                kwp = kwp.toDoubleOrNull() ?: current.installation.kwp,
                azimuthDeg = azimuth.toDoubleOrNull() ?: current.installation.azimuthDeg,
                tiltDeg = tilt.toDoubleOrNull() ?: current.installation.tiltDeg,
                lossesPercent = losses.toDoubleOrNull() ?: current.installation.lossesPercent,
                shadingLevel = shading,
                pacMaxW = pacMax.toDoubleOrNull()
            )
            container.configRepository.saveInstallation(model)
            _state.value = UiState.Success(current.copy(installation = model, message = "Paramètres enregistrés"))
        }
    }

    fun saveConfig(
        provider: String,
        monthlyCalibrationEnabled: Boolean,
        notificationsEnabled: Boolean
    ) {
        viewModelScope.launch {
            val current = (_state.value as? UiState.Success)?.data ?: return@launch
            val updated = current.config.copy(
                provider = provider,
                monthlyCalibrationEnabled = monthlyCalibrationEnabled,
                notificationsEnabled = notificationsEnabled
            )
            container.configRepository.saveAppConfig(updated)
            _state.value = UiState.Success(current.copy(config = updated, message = "Configuration enregistrée"))
        }
    }

    fun saveAll(
        lat: String,
        lon: String,
        kwp: String,
        azimuth: String,
        tilt: String,
        losses: String,
        shading: ShadingLevel,
        pacMax: String,
        provider: String,
        monthlyCalibrationEnabled: Boolean,
        notificationsEnabled: Boolean
    ) {
        viewModelScope.launch {
            val current = (_state.value as? UiState.Success)?.data ?: return@launch
            val installation = current.installation.copy(
                lat = lat.toDoubleOrNull() ?: current.installation.lat,
                lon = lon.toDoubleOrNull() ?: current.installation.lon,
                kwp = kwp.toDoubleOrNull() ?: current.installation.kwp,
                azimuthDeg = azimuth.toDoubleOrNull() ?: current.installation.azimuthDeg,
                tiltDeg = tilt.toDoubleOrNull() ?: current.installation.tiltDeg,
                lossesPercent = losses.toDoubleOrNull() ?: current.installation.lossesPercent,
                shadingLevel = shading,
                pacMaxW = pacMax.toDoubleOrNull()
            )
            val config = current.config.copy(
                provider = provider.ifBlank { current.config.provider },
                monthlyCalibrationEnabled = monthlyCalibrationEnabled,
                notificationsEnabled = notificationsEnabled
            )
            container.configRepository.saveInstallation(installation)
            container.configRepository.saveAppConfig(config)
            _state.value = UiState.Success(
                current.copy(
                    installation = installation,
                    config = config,
                    message = "Paramètres sauvegardés"
                )
            )
        }
    }

    fun importCsv(csvRaw: String) {
        viewModelScope.launch {
            val current = (_state.value as? UiState.Success)?.data ?: return@launch
            runCatching {
                val imported = container.importEnphaseCsvUseCase(csvRaw)
                val existingForecasts = imported.flatMap {
                    container.getForecastUseCase(it.date)
                }
                val calibrations = container.calibratePrUseCase(imported, existingForecasts, byMonth = true)
                container.configRepository.saveCalibrations(calibrations)
                imported.size
            }.onSuccess {
                _state.value = UiState.Success(current.copy(message = "Import terminé: $it jours"))
            }.onFailure {
                _state.value = UiState.Success(current.copy(message = "Import échoué: ${it.message}"))
            }
        }
    }
}
