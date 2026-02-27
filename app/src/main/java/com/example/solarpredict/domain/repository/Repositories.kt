package com.example.solarpredict.domain.repository

import com.example.solarpredict.domain.model.ActualProductionDay
import com.example.solarpredict.domain.model.AppConfig
import com.example.solarpredict.domain.model.Calibration
import com.example.solarpredict.domain.model.ForecastDay
import com.example.solarpredict.domain.model.Installation
import com.example.solarpredict.domain.model.Scenario
import com.example.solarpredict.domain.model.WeatherHourlyPoint
import java.time.LocalDate

interface WeatherRepository {
    suspend fun fetchWeatherHourly(
        startDate: LocalDate,
        endDate: LocalDate,
        lat: Double,
        lon: Double
    ): Map<LocalDate, List<WeatherHourlyPoint>>
}

interface ForecastRepository {
    suspend fun getForecast(date: LocalDate, scenario: Scenario): ForecastDay?
    suspend fun getForecastsForDate(date: LocalDate): List<ForecastDay>
    suspend fun saveForecasts(forecasts: List<ForecastDay>)
    suspend fun getForecastsBetween(startDate: LocalDate, endDate: LocalDate, scenario: Scenario): List<ForecastDay>
}

interface ActualRepository {
    suspend fun saveActuals(items: List<ActualProductionDay>)
    suspend fun getActualBetween(startDate: LocalDate, endDate: LocalDate): List<ActualProductionDay>
    suspend fun importFromCsv(csvRaw: String): List<ActualProductionDay>
}

interface ConfigRepository {
    suspend fun getInstallation(): Installation
    suspend fun saveInstallation(installation: Installation)
    suspend fun getCalibrations(): List<Calibration>
    suspend fun saveCalibrations(calibrations: List<Calibration>)
    suspend fun getAppConfig(): AppConfig
    suspend fun saveAppConfig(config: AppConfig)
}
