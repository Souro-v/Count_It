package com.example.count_it.api

import retrofit2.http.GET
import retrofit2.http.Query

data class FoodApiResponse(
    val product: FoodProduct? = null
)

data class FoodProduct(
    val product_name: String? = null,
    val energy_kcal_100g: Double? = null,
    val proteins_100g: Double? = null,
    val carbohydrates_100g: Double? = null,
    val fat_100g: Double? = null
)

interface OpenFoodFactsApi {
    @GET("api/v0/product/{barcode}")
    suspend fun getFoodByBarcode(
        @Query("barcode") barcode: String
    ): FoodApiResponse

    @GET("api/v1/search")
    suspend fun searchFood(
        @Query("query") query: String
    ): SearchResponse
}

data class SearchResponse(
    val products: List<FoodProduct>? = null
)