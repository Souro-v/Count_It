package com.example.count_it.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.count_it.data.FoodEntity
import com.example.count_it.data.NutritionInfo
import com.example.count_it.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// UI State data class to hold all screen states
data class FoodUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val nutritionInfo: NutritionInfo? = null,
    val capturedImage: Bitmap? = null,
    val todayCalories: Int = 0,
    val searchQuery: String = ""
)

class FoodViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FoodRepository(application)

    // UI State
    private val _uiState = MutableStateFlow(FoodUiState())
    val uiState: StateFlow<FoodUiState> = _uiState.asStateFlow()

    // Today's food list
    private val _todayFood = MutableStateFlow<List<FoodEntity>>(emptyList())
    val todayFood: StateFlow<List<FoodEntity>> = _todayFood.asStateFlow()

    // All food history
    private val _allFood = MutableStateFlow<List<FoodEntity>>(emptyList())
    val allFood: StateFlow<List<FoodEntity>> = _allFood.asStateFlow()

    init {
        // Load all data when app starts
        loadTodayFood()
        loadAllFood()
        loadTodayCalories()
    }

    // ==================== Load Data ====================

    private fun loadTodayFood() {
        viewModelScope.launch {
            repository.getTodayFood().collect { foods ->
                _todayFood.value = foods
            }
        }
    }

    private fun loadAllFood() {
        viewModelScope.launch {
            repository.getAllFood().collect { foods ->
                _allFood.value = foods
            }
        }
    }

    private fun loadTodayCalories() {
        viewModelScope.launch {
            repository.getTodayTotalCalories().collect { calories ->
                _uiState.value = _uiState.value.copy(
                    todayCalories = calories ?: 0
                )
            }
        }
    }

    // ==================== Camera Actions ====================

    // Called when user captures an image from camera
    fun onImageCaptured(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(
            capturedImage = bitmap,
            isLoading = true,
            errorMessage = null
        )
    }

    // ==================== Search Food ====================

    // Search food by name from API
    fun searchFood(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                searchQuery = query
            )

            val result = repository.searchFoodByName(query)

            result.fold(
                onSuccess = { nutritionInfo ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        nutritionInfo = nutritionInfo,
                        errorMessage = if (nutritionInfo == null) "No food found" else null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error: ${error.message}"
                    )
                }
            )
        }
    }

    // ==================== Save Food ====================

    // Save food item to local database
    fun saveFood(nutritionInfo: NutritionInfo, imageUrl: String? = null) {
        viewModelScope.launch {
            val foodEntity = FoodEntity(
                foodName = nutritionInfo.name,
                calories = nutritionInfo.calories,
                protein = nutritionInfo.protein,
                carbs = nutritionInfo.carbs,
                fat = nutritionInfo.fat,
                imageUrl = imageUrl
            )
            repository.insertFood(foodEntity)

            // Reset state after saving
            _uiState.value = _uiState.value.copy(
                nutritionInfo = null,
                capturedImage = null,
                searchQuery = ""
            )
        }
    }

    // ==================== Delete Food ====================

    // Delete single food item
    fun deleteFood(food: FoodEntity) {
        viewModelScope.launch {
            repository.deleteFood(food)
        }
    }

    // Delete all food history
    fun deleteAllFood() {
        viewModelScope.launch {
            repository.deleteAllFood()
        }
    }

    // ==================== Clear States ====================

    // Clear error message from UI
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // Clear result and reset captured image
    fun clearResult() {
        _uiState.value = _uiState.value.copy(
            nutritionInfo = null,
            capturedImage = null
        )
    }
}