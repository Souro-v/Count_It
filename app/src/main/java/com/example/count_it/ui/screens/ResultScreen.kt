package com.example.count_it.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.count_it.data.NutritionInfo
import com.example.count_it.viewmodel.FoodViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: FoodViewModel,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Pre-fill search with AI detected food name
    var searchQuery by remember(uiState.detectedFoodName) {
        mutableStateOf(uiState.detectedFoodName ?: "")
    }

    // Show success dialog state
    var showSaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nutrition Info 🥗",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Show captured food image
            uiState.capturedImage?.let { bitmap ->
                CapturedImageCard(bitmap = bitmap)
            }

            // Show AI detection result if available
            uiState.detectedFoodName?.let { detectedName ->
                AIDetectionCard(
                    detectedName = detectedName,
                    confidence = uiState.detectionConfidence
                )
            }

            // Loading indicator while AI analyzing
            if (uiState.isLoading) {
                AILoadingCard()
            }

            // Manual food search section
            SearchFoodSection(
                query = searchQuery,
                isLoading = uiState.isLoading,
                onQueryChange = { searchQuery = it },
                onSearch = {
                    if (searchQuery.isNotBlank()) {
                        viewModel.searchFood(searchQuery)
                    }
                }
            )

            // Show error message if any
            uiState.errorMessage?.let { error ->
                ErrorCard(
                    message = error,
                    onDismiss = { viewModel.clearError() }
                )
            }

            // Show nutrition results
            uiState.nutritionInfo?.let { nutrition ->
                NutritionResultCard(nutritionInfo = nutrition)

                // Save food button
                Button(
                    onClick = {
                        viewModel.saveFood(nutrition)
                        showSaveDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Save to Today's Log ✅",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Success dialog after saving
        if (showSaveDialog) {
            SaveSuccessDialog(
                foodName = uiState.nutritionInfo?.name ?: "",
                onDismiss = {
                    showSaveDialog = false
                    onNavigateHome()
                }
            )
        }
    }
}

// AI detection result card
@Composable
fun AIDetectionCard(
    detectedName: String,
    confidence: Float
) {
    val confidencePercent = (confidence * 100).roundToInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "🤖", fontSize = 28.sp)
                Column {
                    Text(
                        text = "AI Detected",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = detectedName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Confidence badge
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        confidencePercent >= 70 -> Color(0xFF4CAF50)
                        confidencePercent >= 40 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                )
            ) {
                Text(
                    text = "$confidencePercent%",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// Loading card while AI is analyzing
@Composable
fun AILoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp
            )
            Column {
                Text(
                    text = "AI Analyzing...",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Detecting food and fetching nutrition info",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// Captured food image card
@Composable
fun CapturedImageCard(bitmap: Bitmap) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Captured food image",
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

// Manual food name search section
@Composable
fun SearchFoodSection(
    query: String,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Search Food Manually",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )

            Text(
                text = "AI detected name is pre-filled. Edit if needed.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search input field
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(text = "e.g. rice, apple, burger...")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Search button
                IconButton(
                    onClick = onSearch,
                    enabled = !isLoading && query.isNotBlank(),
                    modifier = Modifier
                        .size(52.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (query.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Nutrition result card showing calories and macros
@Composable
fun NutritionResultCard(nutritionInfo: NutritionInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Food name and serving size
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = nutritionInfo.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Per ${nutritionInfo.servingSize}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
                Text(text = "🍽️", fontSize = 32.sp)
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
            )

            // Big calorie display
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${nutritionInfo.calories}",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "CALORIES (kcal)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
            )

            // Macros row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroItem(
                    label = "Protein",
                    value = "${nutritionInfo.protein}g",
                    color = Color(0xFF4CAF50),
                    emoji = "💪"
                )
                MacroItem(
                    label = "Carbs",
                    value = "${nutritionInfo.carbs}g",
                    color = Color(0xFF2196F3),
                    emoji = "⚡"
                )
                MacroItem(
                    label = "Fat",
                    value = "${nutritionInfo.fat}g",
                    color = Color(0xFFFF9800),
                    emoji = "🧈"
                )
            }
        }
    }
}

// Single macro nutrient display item
@Composable
fun MacroItem(
    label: String,
    value: String,
    color: Color,
    emoji: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = emoji, fontSize = 24.sp)
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
        )
    }
}

// Error message card
@Composable
fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "⚠️", fontSize = 20.sp)
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 14.sp
                )
            }
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Dismiss",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Dialog shown after successfully saving food
@Composable
fun SaveSuccessDialog(
    foodName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text(text = "✅", fontSize = 32.sp) },
        title = {
            Text(
                text = "Saved Successfully!",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "$foodName has been added to today's log.",
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(text = "Go to Home")
            }
        }
    )
}