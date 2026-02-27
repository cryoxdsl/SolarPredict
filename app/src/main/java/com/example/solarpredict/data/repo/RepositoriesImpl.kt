package com.example.solarpredict.data.repo

import com.example.solarpredict.data.api.OpenMeteoService
import com.example.solarpredict.data.csv.EnphaseCsvParser
import com.example.solarpredict.data.db.ActualDao
import com.example.solarpredict.data.db.AppConfigDao
import com.example.solarpredict.data.db.CalibrationDao
import com.example.solarpredict.data.db.ForecastDao
import com.example.solarpredict.data.db.InstallationDao
import com.example.solarpredict.data.mappers.toDomain
import com.example.solarpredict.data.mappers.toEntity
import com.example.solarpredict.domain.model.ActualProductionDay
import com.example.solarpredict.domain.model.AppConfig
import com.example.solarpredict.domain.model.Calibration
import com.example.solarpredict.domain.model.ForecastDay
import com.example.solarpredict.domain.model.Installation
import com.example.solarpredict.domain.model.Scenario
import com.example.solarpredict.domain.model.ShadingLevel
import com.example.solarpredict.domain.model.WeatherHourlyPoint
import com.example.solarpredict.domain.repository.ActualRepository
import com.example.solarpredict.domain.repository.ConfigRepository
import com.example.solarpredict.domain.repository.ForecastRepository
import com.example.solarpredict.domain.repository.WeatherRepository
import java.time.LocalDate

class WeatherRepositoryImpl(
    private val api: OpenMeteoService
) : WeatherRepository {
    override suspend fun fetchWeatherHourly(
        startDate: LocalDate,
        endDate: LocalDate,
        lat: Double,
        lon: Double
    ): Map<LocalDate, List<WeatherHourlyPoint>> {
        val response = api.getHourlyForecast(
            latitude = lat,
            longitude = lon,
            startDate = startDate.toString(),
            endDate = endDate.toString()
        )
        val out = mutableMapOf<LocalDate, MutableList<WeatherHourlyPoint>>()
        response.hourly.time.indices.forEach { i ->
            val iso = response.hourly.time[i]
            val date = LocalDate.parse(iso.substring(0, 10))
            val hour = iso.substring(11, 13).toIntOrNull() ?: 0
            out.getOrPut(date) { mutableListOf() }.add(
                WeatherHourlyPoint(
                    dateTimeIso = iso,
                    hour = hour,
                    cloudCover = response.hourly.cloudCover.getOrElse(i) { 0.0 },
                    temperatureC = response.hourly.temperature2m.getOrElse(i) { 25.0 },
                    precipitationProbability = response.hourly.precipitationProbability.getOrElse(i) { 0.0 },
                    shortwaveRadiation = response.hourly.shortwaveRadiation?.getOrNull(i)
                )
            )
        }
        return out
    }
}

class ForecastRepositoryImpl(
    private val dao: ForecastDao
) : ForecastRepository {
    override suspend fun getForecast(date: LocalDate, scenario: Scenario): ForecastDay? =
        dao.getByDateScenario(date.toString(), scenario.name)?.toDomain()

    override suspend fun getForecastsForDate(date: LocalDate): List<ForecastDay> =
        dao.getByDate(date.toString()).map { it.toDomain() }

    override suspend fun saveForecasts(forecasts: List<ForecastDay>) {
        dao.upsertAll(forecasts.map { it.toEntity() })
    }

    override suspend fun getForecastsBetween(startDate: LocalDate, endDate: LocalDate, scenario: Scenario): List<ForecastDay> =
        dao.getBetween(startDate.toString(), endDate.toString(), scenario.name).map { it.toDomain() }
}

class ActualRepositoryImpl(
    private val dao: ActualDao,
    private val parser: EnphaseCsvParser
) : ActualRepository {
    override suspend fun saveActuals(items: List<ActualProductionDay>) {
        dao.upsertAll(items.map { it.toEntity() })
    }

    override suspend fun getActualBetween(startDate: LocalDate, endDate: LocalDate): List<ActualProductionDay> =
        dao.getBetween(startDate.toString(), endDate.toString()).map { it.toDomain() }

    override suspend fun importFromCsv(csvRaw: String): List<ActualProductionDay> = parser.parse(csvRaw)
}

class ConfigRepositoryImpl(
    private val installationDao: InstallationDao,
    private val calibrationDao: CalibrationDao,
    private val appConfigDao: AppConfigDao
) : ConfigRepository {
    override suspend fun getInstallation(): Installation {
        return installationDao.get()?.toDomain() ?: Installation(
            lat = 48.85,
            lon = 2.35,
            kwp = 6.0,
            azimuthDeg = 180.0,
            tiltDeg = 30.0,
            lossesPercent = 14.0,
            shadingLevel = ShadingLevel.NONE,
            pacMaxW = 5000.0
        )
    }

    override suspend fun saveInstallation(installation: Installation) {
        installationDao.upsert(installation.toEntity())
    }

    override suspend fun getCalibrations(): List<Calibration> {
        val existing = calibrationDao.getAll()
        if (existing.isEmpty()) {
            val defaults = (1..12).map { Calibration(it, 1.0) }
            calibrationDao.upsertAll(defaults.map { it.toEntity() })
            return defaults
        }
        return existing.map { it.toDomain() }
    }

    override suspend fun saveCalibrations(calibrations: List<Calibration>) {
        calibrationDao.upsertAll(calibrations.map { it.toEntity() })
    }

    override suspend fun getAppConfig(): AppConfig {
        return appConfigDao.get()?.toDomain() ?: AppConfig()
    }

    override suspend fun saveAppConfig(config: AppConfig) {
        appConfigDao.upsert(config.toEntity())
    }
}
