package com.example.solarpredict.domain

import com.example.solarpredict.domain.model.Calibration
import com.example.solarpredict.domain.model.Installation
import com.example.solarpredict.domain.model.ShadingLevel
import com.example.solarpredict.domain.model.WeatherHourlyPoint
import com.example.solarpredict.domain.usecase.ComputePvForecastUseCase
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ComputePvForecastUseCaseTest {
    private val useCase = ComputePvForecastUseCase()

    @Test
    fun clearSkyProducesEnergy() {
        val weather = (0..23).map {
            WeatherHourlyPoint("2026-06-01T${"%02d".format(it)}:00", it, 5.0, 24.0, 0.0, 700.0)
        }
        val installation = Installation(45.0, 2.0, 6.0, 180.0, 30.0, 10.0, ShadingLevel.NONE, null)
        val out = useCase(LocalDate.of(2026, 6, 1), weather, installation, listOf(Calibration(6, 1.0)))
        val realistic = out.first { it.scenario.name == "REALISTIC" }
        assertTrue(realistic.dailyKwh > 0.0)
    }

    @Test
    fun cloudyDayLowerThanClearSky() {
        val clear = (0..23).map {
            WeatherHourlyPoint("2026-06-01T${"%02d".format(it)}:00", it, 0.0, 25.0, 0.0, 750.0)
        }
        val cloudy = (0..23).map {
            WeatherHourlyPoint("2026-06-01T${"%02d".format(it)}:00", it, 95.0, 25.0, 0.0, 250.0)
        }
        val installation = Installation(45.0, 2.0, 6.0, 180.0, 30.0, 10.0, ShadingLevel.NONE, null)
        val clearOut = useCase(LocalDate.of(2026, 6, 1), clear, installation, listOf(Calibration(6, 1.0)))
        val cloudyOut = useCase(LocalDate.of(2026, 6, 1), cloudy, installation, listOf(Calibration(6, 1.0)))
        val clearKwh = clearOut.first { it.scenario.name == "REALISTIC" }.dailyKwh
        val cloudyKwh = cloudyOut.first { it.scenario.name == "REALISTIC" }.dailyKwh
        assertTrue(cloudyKwh < clearKwh)
    }

    @Test
    fun clippingLimitsPeak() {
        val weather = (0..23).map {
            WeatherHourlyPoint("2026-06-01T${"%02d".format(it)}:00", it, 0.0, 20.0, 0.0, 1000.0)
        }
        val installation = Installation(45.0, 2.0, 10.0, 180.0, 30.0, 5.0, ShadingLevel.NONE, 3000.0)
        val out = useCase(LocalDate.of(2026, 6, 1), weather, installation, listOf(Calibration(6, 1.0)))
        val realistic = out.first { it.scenario.name == "REALISTIC" }
        assertTrue(realistic.peakW <= 3000.0)
        assertTrue(realistic.clippingLikely)
    }
}
