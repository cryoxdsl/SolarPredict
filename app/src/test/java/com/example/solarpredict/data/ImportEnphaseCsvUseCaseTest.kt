package com.example.solarpredict.data

import com.example.solarpredict.data.csv.EnphaseCsvParser
import com.example.solarpredict.domain.model.ActualProductionDay
import com.example.solarpredict.domain.repository.ActualRepository
import com.example.solarpredict.domain.usecase.ImportEnphaseCsvUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ImportEnphaseCsvUseCaseTest {
    @Test
    fun importsHourlyAndDailyFormats() = runTest {
        val parser = EnphaseCsvParser()
        val repo = FakeActualRepository(parser)
        val useCase = ImportEnphaseCsvUseCase(repo)

        val hourlyCsv = "Date,Hour,Energy\n2026-01-10,10:00,1.2\n2026-01-10,11:00,1.0"
        val dailyCsv = "Date;Energy(kWh)\n10/01/2026;3,5"

        val hourly = useCase(hourlyCsv)
        val daily = useCase(dailyCsv)

        assertEquals(1, hourly.size)
        assertEquals(2.2, hourly.first().dailyKwh, 0.01)
        assertEquals(1, daily.size)
        assertEquals(3.5, daily.first().dailyKwh, 0.01)
    }

    private class FakeActualRepository(
        private val parser: EnphaseCsvParser
    ) : ActualRepository {
        val stored = mutableListOf<ActualProductionDay>()

        override suspend fun saveActuals(items: List<ActualProductionDay>) {
            stored.addAll(items)
        }

        override suspend fun getActualBetween(startDate: LocalDate, endDate: LocalDate): List<ActualProductionDay> = stored

        override suspend fun importFromCsv(csvRaw: String): List<ActualProductionDay> = parser.parse(csvRaw)
    }
}
