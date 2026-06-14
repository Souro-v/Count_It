package com.example.count_it.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.count_it.data.FoodEntity
import com.example.count_it.viewmodel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: FoodViewModel,
    onNavigateBack: () -> Unit
) {
    val allFood by viewModel.allFood.collectAsState()

    // Show confirm delete all dialog
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    // Group food items by date
    val groupedFood = allFood.groupBy { food ->
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        dateFormat.format(Date(food.timestamp))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Food History 📜",
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
                actions = {
                    // Delete all history button
                    if (allFood.isNotEmpty()) {
                        IconButton(onClick = { showDeleteAllDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Delete All",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->

        if (allFood.isEmpty()) {
            // Show empty state
            EmptyHistoryContent()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Total summary card at top
                item {
                    HistorySummaryCard(allFood = allFood)
                }

                // Group food items by date
                groupedFood.forEach { (date, foods) ->
                    // Date header
                    item {
                        DateHeader(
                            date = date,
                            totalCalories = foods.sumOf { it.calories }
                        )
                    }

                    // Food items for that date
                    items(foods) { food ->
                        HistoryFoodItemCard(
                            food = food,
                            onDelete = { viewModel.deleteFood(food) }
                        )
                    }
                }
            }
        }

        // Confirm delete all dialog
        if (showDeleteAllDialog) {
            DeleteAllConfirmDialog(
                onConfirm = {
                    viewModel.deleteAllFood()
                    showDeleteAllDialog = false
                },
                onDismiss = { showDeleteAllDialog = false }
            )
        }
    }
}

// Summary card showing all time stats
@Composable
fun HistorySummaryCard(allFood: List<FoodEntity>) {
    val totalCalories = allFood.sumOf { it.calories }
    val totalProtein = allFood.sumOf { it.protein }
    val totalCarbs = allFood.sumOf { it.carbs }
    val totalFat = allFood.sumOf { it.fat }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "All Time Summary",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total meals count
                SummaryStatItem(
                    label = "Meals",
                    value = "${allFood.size}",
                    emoji = "🍽️"
                )
                // Total calories
                SummaryStatItem(
                    label = "Calories",
                    value = "$totalCalories",
                    emoji = "🔥"
                )
                // Total protein
                SummaryStatItem(
                    label = "Protein",
                    value = "${String.format(Locale.getDefault(), "%.1f", totalProtein)}g",
                    emoji = "💪"
                )
                // Total carbs
                SummaryStatItem(
                    label = "Carbs",
                    value = "${String.format(Locale.getDefault(), "%.1f", totalCarbs)}g",
                    emoji = "⚡"
                )
                // Total fat
                SummaryStatItem(
                    label = "Fat",
                    value = "${String.format(Locale.getDefault(), "%.1f", totalFat)}g",emoji = "🧈"
                )
            }
        }
    }
}

// Single stat item in summary card
@Composable
fun SummaryStatItem(
    label: String,
    value: String,
    emoji: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
        )
    }
}

// Date section header with total calories
@Composable
fun DateHeader(
    date: String,
    totalCalories: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "$totalCalories kcal",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

// History food item card with delete option
@Composable
fun HistoryFoodItemCard(
    food: FoodEntity,
    onDelete: () -> Unit
) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val time = timeFormat.format(Date(food.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "🍽️", fontSize = 28.sp)
                Column {
                    Text(
                        text = food.foodName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "P: ${food.protein}g • C: ${food.carbs}g • F: ${food.fat}g",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = time,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            // Calories and delete button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${food.calories}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "kcal",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Empty state when no history available
@Composable
fun EmptyHistoryContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "📭", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Food History Yet!",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start logging your meals to see history here.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

// Confirm dialog before deleting all history
@Composable
fun DeleteAllConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text(text = "🗑️", fontSize = 32.sp) },
        title = {
            Text(
                text = "Delete All History?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "This will permanently delete all your food history. This action cannot be undone.",
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(text = "Delete All")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}