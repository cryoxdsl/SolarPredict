package com.example.solarpredict.usecase

import com.example.solarpredict.domain.model.ActualProductionDay
import com.example.solarpredict.domain.model.ForecastDay
import com.example.solarpredict.domain.model.Scenario
import com.example.solarpredict.domain.usecase.CalibratePrUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CalibratePrUseCaseTest {
    @Test
    fun computesMonthlyPr() {
        val useCase = CalibratePrUseCase()
        val actual = listOf(
            ActualProductionDay(LocalDate.of(2026, 1, 1), null, 4.0),
            ActualProductionDay(LocalDate.of(2026, 1, 2), null, 6.0)
        )
        val forecast = listOf(
            ForecastDay(LocalDate.of(2026, 1, 1), Scenario.REALISTIC, List(24) { 0.0 }, 5.0, 0.0, 0, false),
            ForecastDay(LocalDate.of(2026, 1, 2), Scenario.REALISTIC, List(24) { 0.0 }, 5.0, 0.0, 0, false)
        )

        val out = useCase(actual, forecast, byMonth = true)
        val jan = out.first { it.month == 1 }
        assertEquals(1.0, jan.prFactor, 0.001)
        assertTrue(out.size == 12)
    }
}
