package com.example.solarpredict.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        InstallationEntity::class,
        ForecastEntity::class,
        ActualProductionEntity::class,
        CalibrationEntity::class,
        AppConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun installationDao(): InstallationDao
    abstract fun forecastDao(): ForecastDao
    abstract fun actualDao(): ActualDao
    abstract fun calibrationDao(): CalibrationDao
    abstract fun appConfigDao(): AppConfigDao
}
