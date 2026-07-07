package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_records ORDER BY date DESC")
    fun getAllRecordsFlow(): Flow<List<WeightRecord>>

    @Query("SELECT * FROM weight_records ORDER BY date ASC")
    fun getAllRecordsFlowAsc(): Flow<List<WeightRecord>>

    @Query("SELECT * FROM weight_records WHERE date = :date LIMIT 1")
    suspend fun getRecordByDate(date: String): WeightRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: WeightRecord)

    @Delete
    suspend fun deleteRecord(record: WeightRecord)

    @Query("DELETE FROM weight_records WHERE date = :date")
    suspend fun deleteRecordByDate(date: String)
}
