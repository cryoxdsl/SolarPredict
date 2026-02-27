package com.example.solarpredict.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromDoubleList(value: List<Double>?): String? = value?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toDoubleList(value: String?): List<Double>? = value?.let { Json.decodeFromString<List<Double>>(it) }
}
