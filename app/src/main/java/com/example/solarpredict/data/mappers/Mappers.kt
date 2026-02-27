package com.example.solarpredict.data.mappers

import com.example.solarpredict.data.db.ActualProductionEntity
import com.example.solarpredict.data.db.AppConfigEntity
import com.example.solarpredict.data.db.CalibrationEntity
import com.example.solarpredict.data.db.ForecastEntity
import com.example.solarpredict.data.db.InstallationEntity
import com.example.solarpredict.domain.model.ActualProductionDay
import com.example.solarpredict.domain.model.AppConfig
import com.example.solarpredict.domain.model.Calibration
import com.example.solarpredict.domain.model.ForecastDay
import com.example.solarpredict.domain.model.Installation
import com.example.solarpredict.domain.model.Scenario
import com.example.solarpredict.domain.model.ShadingLevel
import java.time.LocalDate

fun InstallationEntity.toDomain() = Installation(
    lat = lat,
    lon = lon,
    kwp = kwp,
    azimuthDeg = azimuthDeg,
    tiltDeg = tiltDeg,
    lossesPercent = lossesPercent,
    shadingLevel = ShadingLevel.valueOf(shading),
    pacMaxW = pacMaxW,
    tempCoeff = tempCoeff
)

fun Installation.toEntity() = InstallationEntity(
    lat = lat,
    lon = lon,
    kwp = kwp,
    azimuthDeg = azimuthDeg,
    tiltDeg = tiltDeg,
    lossesPercent = lossesPercent,
    shading = shadingLevel.name,
    pacMaxW = pacMaxW,
    tempCoeff = tempCoeff
)

fun ForecastEntity.toDomain() = ForecastDay(
    date = LocalDate.parse(date),
    scenario = Scenario.valueOf(scenario),
    hourlyPowerW = hourlyPowers,
    dailyKwh = dailyKwh,
    peakW = peakW,
    peakHour = peakHour,
    clippingLikely = clippingLikely
)

fun ForecastDay.toEntity() = ForecastEntity(
    date = date.toString(),
    scenario = scenario.name,
    hourlyPowers = hourlyPowerW,
    dailyKwh = dailyKwh,
    peakW = peakW,
    peakHour = peakHour,
    clippingLikely = clippingLikely
)

fun ActualProductionEntity.toDomain() = ActualProductionDay(
    date = LocalDate.parse(date),
    hourlyPowerW = hourlyPowers,
    dailyKwh = dailyKwh
)

fun ActualProductionDay.toEntity() = ActualProductionEntity(
    date = date.toString(),
    hourlyPowers = hourlyPowerW,
    dailyKwh = dailyKwh
)

fun CalibrationEntity.toDomain() = Calibration(month, prFactor)
fun Calibration.toEntity() = CalibrationEntity(month, prFactor)

fun AppConfigEntity.toDomain() = AppConfig(
    refreshTime = refreshTime,
    provider = provider,
    monthlyCalibrationEnabled = monthlyCalibrationEnabled,
    notificationsEnabled = notificationsEnabled
)

fun AppConfig.toEntity() = AppConfigEntity(
    refreshTime = refreshTime,
    provider = provider,
    monthlyCalibrationEnabled = monthlyCalibrationEnabled,
    notificationsEnabled = notificationsEnabled
)
