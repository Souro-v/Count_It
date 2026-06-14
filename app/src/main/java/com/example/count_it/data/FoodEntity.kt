package com.example.count_it.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_history")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val foodName: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val imageUrl: String?,
    val timestamp: Long = System.currentTimeMillis()
)