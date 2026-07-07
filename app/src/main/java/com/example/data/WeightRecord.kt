package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_records")
data class WeightRecord(
    @PrimaryKey
    val date: String, // format: "yyyy-MM-dd"
    val weight: Double, // in kg
    val fatPercentage: Double? = null, // body fat %
    val note: String? = null, // text notes/mood/tags
    val timestamp: Long = System.currentTimeMillis()
)
