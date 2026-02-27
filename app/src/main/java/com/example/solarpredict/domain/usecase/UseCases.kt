package com.example.solarpredict.domain.usecase

import com.example.solarpredict.domain.model.ActualProductionDay
import com.example.solarpredict.domain.model.Calibration
import com.example.solarpredict.domain.model.ForecastDay
import com.example.solarpredict.domain.model.HistoryItem
import com.example.solarpredict.domain.model.Installation
import com.example.solarpredict.domain.model.Metrics
import com.example.solarpredict.domain.model.Scenario
import com.example.solarpredict.domain.model.ShadingLevel
import com.example.solarpredict.domain.model.WeatherHourlyPoint
import com.example.solarpredict.domain.repository.ActualRepository
import com.example.solarpredict.domain.repository.ForecastRepository
import com.example.solarpredict.domain.repository.WeatherRepository
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max

class FetchWeatherHourlyUseCase(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(
        startDate: LocalDate,
        endDate: LocalDate,
        lat: Double,
        lon: Double
    ) = weatherRepository.fetchWeatherHourly(startDate, endDate, lat, lon)
}

class ImportEnphaseCsvUseCase(
    private val actualRepository: ActualRepository
) {
    suspend operator fun invoke(csvRaw: String): List<ActualProductionDay> =
        actualRepository.importFromCsv(csvRaw).also { actualRepository.saveActuals(it) }
}

class CalibratePrUseCase {
    operator fun invoke(
        actuals: List<ActualProductionDay>,
        theoretical: List<ForecastDay>,
        byMonth: Boolean = true
    ): List<Calibration> {
        val grouped = mutableMapOf<Int, MutableList<Pair<Double, Double>>>()
        actuals.forEach { actual ->
            val theo = theoretical.find { it.date == actual.date && it.scenario == Scenario.REALISTIC } ?: return@forEach
            val month = if (byMonth) actual.date.monthValue else 0
            grouped.getOrPut(month) { mutableListOf() }.add(theo.dailyKwh to actual.dailyKwh)
        }
        if (grouped.isEmpty()) return (1..12).map { Calibration(it, 1.0) }
        return (1..12).map { m ->
            val key = if (byMonth) m else 0
            val values = grouped[key].orEmpty()
            val pr = if (values.isEmpty()) 1.0 else {
                val theo = values.sumOf { it.first }
                val act = values.sumOf { it.second }
                if (theo <= 0.0) 1.0 else (act / theo).coerceIn(0.5, 1.2)
            }
            Calibration(m, pr)
        }
    }
}

class ComputePvForecastUseCase {
    operator fun invoke(
        date: LocalDate,
        weatherHourly: List<WeatherHourlyPoint>,
        installation: Installation,
        calibrations: List<Calibration>
    ): List<ForecastDay> {
        val pr = calibrations.firstOrNull { it.month == date.monthValue }?.prFactor ?: 1.0
        return listOf(
            buildScenario(date, weatherHourly, installation, pr, Scenario.PESSIMISTIC, cloudK = 0.85),
            buildScenario(date, weatherHourly, installation, pr, Scenario.REALISTIC, cloudK = 0.65),
            buildScenario(date, weatherHourly, installation, pr, Scenario.OPTIMISTIC, cloudK = 0.45)
        )
    }

    private fun buildScenario(
        date: LocalDate,
        weatherHourly: List<WeatherHourlyPoint>,
        installation: Installation,
        pr: Double,
        scenario: Scenario,
        cloudK: Double
    ): ForecastDay {
        val day = date.dayOfYear
        val latFactor = cos(installation.lat * PI / 180.0).coerceIn(0.2, 1.0)
        // Max near summer solstice (day ~172), min in winter.
        val seasonal = 0.55 + 0.45 * cos((day - 172) * 2.0 * PI / 365.0)
        val shadingFactor = when (installation.shadingLevel) {
            ShadingLevel.NONE -> 1.0
            ShadingLevel.LIGHT -> 0.9
            ShadingLevel.HEAVY -> 0.75
        }
        val lossesFactor = (1.0 - installation.lossesPercent / 100.0).coerceIn(0.5, 1.0)

        val radiationList = weatherHourly.mapNotNull { it.shortwaveRadiation }
        val radiationMax = radiationList.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

        var clippedHours = 0
        val powers = (0..23).map { h ->
            val w = weatherHourly.firstOrNull { it.hour == h }
            val clearSkyBell = exp(-0.5 * ((h - 12.0) / 3.6).let { it * it }) * (850.0 * latFactor * seasonal)
            val radiationBase = if (w?.shortwaveRadiation != null) {
                (w.shortwaveRadiation / radiationMax) * (1000.0 * seasonal)
            } else clearSkyBell
            val cloud = w?.cloudCover ?: 0.0
            val fCloud = (1.0 - (cloud / 100.0) * cloudK).coerceIn(0.1, 1.0)
            val temp = w?.temperatureC ?: 25.0
            val fTemp = if (temp > 25.0) 1.0 - installation.tempCoeff * (temp - 25.0) else 1.0
            val dcPower = installation.kwp * 1000.0 * (radiationBase / 1000.0) * fCloud * fTemp * lossesFactor * shadingFactor * pr
            val ac = max(0.0, dcPower)
            val clipped = installation.pacMaxW?.let { ac.coerceAtMost(it) } ?: ac
            if (installation.pacMaxW != null && clipped < ac) clippedHours++
            clipped
        }

        val dailyKwh = powers.sum() / 1000.0
        val peakW = powers.maxOrNull() ?: 0.0
        val peakHour = powers.indexOf(peakW)
        return ForecastDay(
            date = date,
            scenario = scenario,
            hourlyPowerW = powers,
            dailyKwh = dailyKwh,
            peakW = peakW,
            peakHour = peakHour,
            clippingLikely = clippedHours >= 2
        )
    }
}

class GetForecastUseCase(
    private val forecastRepository: ForecastRepository
) {
    suspend operator fun invoke(date: LocalDate): List<ForecastDay> =
        forecastRepository.getForecastsForDate(date)
}

class SaveForecastUseCase(
    private val forecastRepository: ForecastRepository
) {
    suspend operator fun invoke(forecasts: List<ForecastDay>) = forecastRepository.saveForecasts(forecasts)
}

class GetHistoryUseCase(
    private val forecastRepository: ForecastRepository,
    private val actualRepository: ActualRepository
) {
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate): List<HistoryItem> {
        val preds = forecastRepository.getForecastsBetween(startDate, endDate, Scenario.REALISTIC)
            .associateBy { it.date }
        val actuals = actualRepository.getActualBetween(startDate, endDate)
        return actuals.mapNotNull { act ->
            val pred = preds[act.date] ?: return@mapNotNull null
            HistoryItem(act.date, pred.dailyKwh, act.dailyKwh)
        }.sortedByDescending { it.date }
    }
}

class ComputeMetricsUseCase {
    operator fun invoke(history: List<HistoryItem>): Metrics {
        if (history.isEmpty()) return Metrics(0.0, 0.0)
        val mape = history.mapNotNull {
            if (it.actualKwh <= 0.01) null else kotlin.math.abs((it.actualKwh - it.predictedKwh) / it.actualKwh)
        }.average() * 100.0
        val bias = history.map { it.predictedKwh - it.actualKwh }.average()
        return Metrics(mapePercent = mape, meanBiasKwh = bias)
    }
}
