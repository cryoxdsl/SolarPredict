package com.example.solarpredict.domain.model

import java.time.LocalDate

enum class Scenario { PESSIMISTIC, REALISTIC, OPTIMISTIC }
enum class ShadingLevel { NONE, LIGHT, HEAVY }

data class Installation(
    val lat: Double,
    val lon: Double,
    val kwp: Double,
    val azimuthDeg: Double,
    val tiltDeg: Double,
    val lossesPercent: Double,
    val shadingLevel: ShadingLevel,
    val pacMaxW: Double?,
    val tempCoeff: Double = 0.004
)

data class WeatherHourlyPoint(
    val dateTimeIso: String,
    val hour: Int,
    val cloudCover: Double,
    val temperatureC: Double,
    val precipitationProbability: Double,
    val shortwaveRadiation: Double?
)

data class ForecastDay(
    val date: LocalDate,
    val scenario: Scenario,
    val hourlyPowerW: List<Double>,
    val dailyKwh: Double,
    val peakW: Double,
    val peakHour: Int,
    val clippingLikely: Boolean
)

data class ActualProductionDay(
    val date: LocalDate,
    val hourlyPowerW: List<Double>?,
    val dailyKwh: Double
)

data class Calibration(
    val month: Int,
    val prFactor: Double
)

data class AppConfig(
    val refreshTime: String = "06:00",
    val provider: String = "open-meteo",
    val monthlyCalibrationEnabled: Boolean = true,
    val notificationsEnabled: Boolean = false
)

data class DaySummary(
    val realistic: ForecastDay?,
    val pessimistic: ForecastDay?,
    val optimistic: ForecastDay?
)

data class HistoryItem(
    val date: LocalDate,
    val predictedKwh: Double,
    val actualKwh: Double
)

data class Metrics(
    val mapePercent: Double,
    val meanBiasKwh: Double
)
