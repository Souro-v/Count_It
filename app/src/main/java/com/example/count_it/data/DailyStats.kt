package com.example.count_it.data

data class DailyStats(
    val date: String,
    val totalCalories: Int,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val itemCount: Int
)