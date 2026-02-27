package com.example.solarpredict

import android.content.Context
import androidx.room.Room
import com.example.solarpredict.data.api.OpenMeteoService
import com.example.solarpredict.data.csv.EnphaseCsvParser
import com.example.solarpredict.data.db.AppDatabase
import com.example.solarpredict.data.repo.ActualRepositoryImpl
import com.example.solarpredict.data.repo.ConfigRepositoryImpl
import com.example.solarpredict.data.repo.ForecastRepositoryImpl
import com.example.solarpredict.data.repo.WeatherRepositoryImpl
import com.example.solarpredict.domain.repository.ActualRepository
import com.example.solarpredict.domain.repository.ConfigRepository
import com.example.solarpredict.domain.repository.ForecastRepository
import com.example.solarpredict.domain.repository.WeatherRepository
import com.example.solarpredict.domain.usecase.CalibratePrUseCase
import com.example.solarpredict.domain.usecase.ComputeMetricsUseCase
import com.example.solarpredict.domain.usecase.ComputePvForecastUseCase
import com.example.solarpredict.domain.usecase.FetchWeatherHourlyUseCase
import com.example.solarpredict.domain.usecase.GetForecastUseCase
import com.example.solarpredict.domain.usecase.GetHistoryUseCase
import com.example.solarpredict.domain.usecase.ImportEnphaseCsvUseCase
import com.example.solarpredict.domain.usecase.SaveForecastUseCase
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

class AppContainer(context: Context) {
    private val db = Room.databaseBuilder(context, AppDatabase::class.java, "solar_predict.db").build()

    private val json = Json { ignoreUnknownKeys = true }
    private val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
    private val http = OkHttpClient.Builder().addInterceptor(logger).build()
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(http)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
    private val openMeteo = retrofit.create(OpenMeteoService::class.java)

    val weatherRepository: WeatherRepository = WeatherRepositoryImpl(openMeteo)
    val forecastRepository: ForecastRepository = ForecastRepositoryImpl(db.forecastDao())
    val actualRepository: ActualRepository = ActualRepositoryImpl(db.actualDao(), EnphaseCsvParser())
    val configRepository: ConfigRepository = ConfigRepositoryImpl(db.installationDao(), db.calibrationDao(), db.appConfigDao())

    val fetchWeatherHourlyUseCase = FetchWeatherHourlyUseCase(weatherRepository)
    val importEnphaseCsvUseCase = ImportEnphaseCsvUseCase(actualRepository)
    val calibratePrUseCase = CalibratePrUseCase()
    val computePvForecastUseCase = ComputePvForecastUseCase()
    val getForecastUseCase = GetForecastUseCase(forecastRepository)
    val saveForecastUseCase = SaveForecastUseCase(forecastRepository)
    val getHistoryUseCase = GetHistoryUseCase(forecastRepository, actualRepository)
    val computeMetricsUseCase = ComputeMetricsUseCase()
}
