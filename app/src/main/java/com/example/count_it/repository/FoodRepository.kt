package com.example.count_it.repository

import com.example.count_it.api.FoodProduct
import com.example.count_it.api.RetrofitClient
import com.example.count_it.data.AppDatabase
import com.example.count_it.data.FoodEntity
import com.example.count_it.data.NutritionInfo
import kotlinx.coroutines.flow.Flow
import android.content.Context

class FoodRepository(context: Context) {

    private val foodDao = AppDatabase.getDatabase(context).foodDao()
    private val foodApi = RetrofitClient.foodApi

    // ==================== Database Operations ====================

    //  food history
    fun getAllFood(): Flow<List<FoodEntity>> {
        return foodDao.getAllFood()
    }

    //  food items
    fun getTodayFood(): Flow<List<FoodEntity>> {
        return foodDao.getTodayFood()
    }

    //  total calories
    fun getTodayTotalCalories(): Flow<Int?> {
        return foodDao.getTodayTotalCalories()
    }

    //  food insert
    suspend fun insertFood(food: FoodEntity) {
        foodDao.insertFood(food)
    }

    // food delete
    suspend fun deleteFood(food: FoodEntity) {
        foodDao.deleteFood(food)
    }

    // সব data delete
    suspend fun deleteAllFood() {
        foodDao.deleteAllFood()
    }

    // ==================== API Operations ====================

    // Food search by name
    suspend fun searchFoodByName(query: String): Result<NutritionInfo?> {
        return try {
            val response = foodApi.searchFood(query)
            val product = response.products?.firstOrNull()
            if (product != null) {
                Result.success(product.toNutritionInfo())
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Helper Functions ====================

    // API response to NutritionInfo to convert
    private fun FoodProduct.toNutritionInfo(): NutritionInfo {
        return NutritionInfo(
            name = product_name ?: "Unknown Food",
            calories = energy_kcal_100g?.toInt() ?: 0,
            protein = proteins_100g ?: 0.0,
            carbs = carbohydrates_100g ?: 0.0,
            fat = fat_100g ?: 0.0,
            servingSize = "100g"
        )
    }

    // NutritionInfo  FoodEntity to convert
    fun NutritionInfo.toFoodEntity(imageUrl: String? = null): FoodEntity {
        return FoodEntity(
            foodName = name,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            imageUrl = imageUrl
        )
    }
}