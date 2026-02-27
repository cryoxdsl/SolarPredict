package com.example.solarpredict

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.solarpredict.worker.ForecastRefreshWorker
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class SolarPredictApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        scheduleDailyWork()
    }

    private fun scheduleDailyWork() {
        val now = LocalDateTime.now()
        val next = now.with(LocalTime.of(6, 0)).let { if (it.isBefore(now)) it.plusDays(1) else it }
        val delay = Duration.between(now, next)

        val request = PeriodicWorkRequestBuilder<ForecastRefreshWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay.toMinutes(), TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_forecast_refresh",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
