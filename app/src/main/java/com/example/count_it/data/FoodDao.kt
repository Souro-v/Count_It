package com.example.count_it.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    // Insert new food item
    @Insert
    suspend fun insertFood(food: FoodEntity)

    // Update existing food item
    @Update
    suspend fun updateFood(food: FoodEntity)

    // Delete food item
    @Delete
    suspend fun deleteFood(food: FoodEntity)

    // Get all food history (as Flow for real-time updates)
    @Query("SELECT * FROM food_history ORDER BY timestamp DESC")
    fun getAllFood(): Flow<List<FoodEntity>>

    // Get food by ID
    @Query("SELECT * FROM food_history WHERE id = :id")
    suspend fun getFoodById(id: Int): FoodEntity?

    // Get today's total calories
    @Query("SELECT SUM(calories) FROM food_history WHERE DATE(timestamp/1000, 'unixepoch') = DATE('now')")
    fun getTodayTotalCalories(): Flow<Int?>

    // Get food items from today
    @Query("SELECT * FROM food_history WHERE DATE(timestamp/1000, 'unixepoch') = DATE('now') ORDER BY timestamp DESC")
    fun getTodayFood(): Flow<List<FoodEntity>>

    // Delete all data
    @Query("DELETE FROM food_history")
    suspend fun deleteAllFood()
}