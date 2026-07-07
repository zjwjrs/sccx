package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.WeightRecord
import com.example.data.WeightRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WeightViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WeightRepository
    private val sharedPrefs = application.getSharedPreferences("weight_tracker_prefs", Context.MODE_PRIVATE)

    val allRecords: StateFlow<List<WeightRecord>>
    val allRecordsAsc: StateFlow<List<WeightRecord>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = WeightRepository(database.weightDao())
        
        allRecords = repository.allRecordsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        allRecordsAsc = repository.allRecordsFlowAsc.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Settings & Profile state
    fun getHeight(): Double {
        return sharedPrefs.getFloat("user_height", 165.0f).toDouble()
    }

    fun saveHeight(height: Double) {
        sharedPrefs.edit().putFloat("user_height", height.toFloat()).apply()
    }

    fun getTargetWeight(): Double {
        return sharedPrefs.getFloat("user_target_weight", 50.0f).toDouble()
    }

    fun saveTargetWeight(targetWeight: Double) {
        sharedPrefs.edit().putFloat("user_target_weight", targetWeight.toFloat()).apply()
    }

    fun getNickName(): String {
        return sharedPrefs.getString("user_nickname", "陈静琳") ?: "陈静琳"
    }

    fun saveNickName(name: String) {
        sharedPrefs.edit().putString("user_nickname", name).apply()
    }

    // Database Actions
    fun addOrUpdateRecord(date: String, weight: Double, fatPercentage: Double? = null, note: String? = null) {
        viewModelScope.launch {
            val record = WeightRecord(
                date = date,
                weight = weight,
                fatPercentage = fatPercentage,
                note = note,
                timestamp = System.currentTimeMillis()
            )
            repository.insertRecord(record)
        }
    }

    fun deleteRecord(record: WeightRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    fun deleteRecordByDate(date: String) {
        viewModelScope.launch {
            repository.deleteRecordByDate(date)
        }
    }

    // Import Demo Data helper for instant beautiful review
    fun importDemoData() {
        viewModelScope.launch {
            val demoRecords = listOf(
                WeightRecord("2026-07-01", 54.2, 23.5, "开启减肥计划啦！加油！"),
                WeightRecord("2026-07-02", 53.8, 23.4, "晨起空腹称重，掉秤挺快~"),
                WeightRecord("2026-07-03", 53.9, null, "吃了顿火锅，稍微有点回弹"),
                WeightRecord("2026-07-04", 53.3, 23.1, "坚持控碳，继续加油"),
                WeightRecord("2026-07-05", 52.8, 22.8, "哇！破53了，太开心了！"),
                WeightRecord("2026-07-06", 52.5, null, "今天跟着视频做了30分钟有氧"),
                WeightRecord("2026-07-07", 52.1, 22.5, "晨跑打卡，体形好看了许多")
            )
            for (record in demoRecords) {
                repository.insertRecord(record)
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            val currentList = allRecords.value
            for (record in currentList) {
                repository.deleteRecord(record)
            }
        }
    }
}
