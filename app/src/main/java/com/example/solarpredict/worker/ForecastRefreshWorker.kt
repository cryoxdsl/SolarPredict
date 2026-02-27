package com.example.solarpredict.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.solarpredict.SolarPredictApplication
import java.time.LocalDate

class ForecastRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val container = (applicationContext as SolarPredictApplication).container
        return runCatching {
            val installation = container.configRepository.getInstallation()
            val calibrations = container.configRepository.getCalibrations()
            val start = LocalDate.now()
            val end = start.plusDays(1)
            val weather = container.fetchWeatherHourlyUseCase(start, end, installation.lat, installation.lon)
            val allForecasts = weather.flatMap { (date, points) ->
                container.computePvForecastUseCase(date, points, installation, calibrations)
            }
            container.saveForecastUseCase(allForecasts)
            Result.success()
        }.getOrElse { Result.retry() }
    }
}
