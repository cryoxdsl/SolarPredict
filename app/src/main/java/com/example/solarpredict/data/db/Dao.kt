package com.example.solarpredict.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.time.LocalDate

@Dao
interface InstallationDao {
    @Query("SELECT * FROM installation WHERE id = 1")
    suspend fun get(): InstallationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: InstallationEntity)
}

@Dao
interface ForecastDao {
    @Query("SELECT * FROM forecast WHERE date = :date AND scenario = :scenario LIMIT 1")
    suspend fun getByDateScenario(date: String, scenario: String): ForecastEntity?

    @Query("SELECT * FROM forecast WHERE date = :date")
    suspend fun getByDate(date: String): List<ForecastEntity>

    @Query("SELECT * FROM forecast WHERE date BETWEEN :startDate AND :endDate AND scenario = :scenario")
    suspend fun getBetween(startDate: String, endDate: String, scenario: String): List<ForecastEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ForecastEntity>)
}

@Dao
interface ActualDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ActualProductionEntity>)

    @Query("SELECT * FROM actual_production WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getBetween(startDate: String, endDate: String): List<ActualProductionEntity>
}

@Dao
interface CalibrationDao {
    @Query("SELECT * FROM calibration")
    suspend fun getAll(): List<CalibrationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CalibrationEntity>)
}

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_config WHERE id = 1")
    suspend fun get(): AppConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AppConfigEntity)
}
