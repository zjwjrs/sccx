package com.example.data

import kotlinx.coroutines.flow.Flow

class WeightRepository(private val weightDao: WeightDao) {
    val allRecordsFlow: Flow<List<WeightRecord>> = weightDao.getAllRecordsFlow()
    val allRecordsFlowAsc: Flow<List<WeightRecord>> = weightDao.getAllRecordsFlowAsc()

    suspend fun getRecordByDate(date: String): WeightRecord? {
        return weightDao.getRecordByDate(date)
    }

    suspend fun insertRecord(record: WeightRecord) {
        weightDao.insertRecord(record)
    }

    suspend fun deleteRecord(record: WeightRecord) {
        weightDao.deleteRecord(record)
    }

    suspend fun deleteRecordByDate(date: String) {
        weightDao.deleteRecordByDate(date)
    }
}
