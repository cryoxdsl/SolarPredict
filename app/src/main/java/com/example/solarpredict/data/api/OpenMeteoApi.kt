package com.example.solarpredict.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class OpenMeteoHourlyDto(
    val time: List<String>,
    @SerialName("cloud_cover") val cloudCover: List<Double>,
    @SerialName("temperature_2m") val temperature2m: List<Double>,
    @SerialName("precipitation_probability") val precipitationProbability: List<Double>,
    @SerialName("shortwave_radiation") val shortwaveRadiation: List<Double>? = null
)

@Serializable
data class OpenMeteoResponseDto(
    val hourly: OpenMeteoHourlyDto
)

interface OpenMeteoService {
    @GET("v1/forecast")
    suspend fun getHourlyForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("hourly") hourly: String = "cloud_cover,temperature_2m,precipitation_probability,shortwave_radiation",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponseDto
}
