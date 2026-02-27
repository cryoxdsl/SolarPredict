package com.example.solarpredict.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installation")
data class InstallationEntity(
    @PrimaryKey val id: Int = 1,
    val lat: Double,
    val lon: Double,
    val kwp: Double,
    val azimuthDeg: Double,
    val tiltDeg: Double,
    val lossesPercent: Double,
    val shading: String,
    val pacMaxW: Double?,
    val tempCoeff: Double
)

@Entity(tableName = "forecast")
data class ForecastEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val scenario: String,
    val hourlyPowers: List<Double>,
    val dailyKwh: Double,
    val peakW: Double,
    val peakHour: Int,
    val clippingLikely: Boolean
)

@Entity(tableName = "actual_production")
data class ActualProductionEntity(
    @PrimaryKey val date: String,
    val hourlyPowers: List<Double>?,
    val dailyKwh: Double
)

@Entity(tableName = "calibration")
data class CalibrationEntity(
    @PrimaryKey val month: Int,
    val prFactor: Double
)

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey val id: Int = 1,
    val refreshTime: String,
    val provider: String,
    val monthlyCalibrationEnabled: Boolean,
    val notificationsEnabled: Boolean
)
