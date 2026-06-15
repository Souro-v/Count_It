package com.example.count_it.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import org.tensorflow.lite.support.image.TensorImage

// Result from AI food detection
data class FoodResult(
    val label: String,
    val confidence: Float
)

class FoodImageAnalyzer(private val context: Context) {

    private var imageClassifier: ImageClassifier? = null

    init {
        setupClassifier()
    }

    // Initialize TFLite classifier with food model
    private fun setupClassifier() {
        try {
            val options = ImageClassifier.ImageClassifierOptions.builder()
                .setMaxResults(5)
                .setScoreThreshold(0.1f)
                .build()

            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                "food_model.tflite",
                options
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Analyze bitmap and return top food results
    fun analyze(bitmap: Bitmap): List<FoodResult> {
        return try {
            val tensorImage = TensorImage.fromBitmap(bitmap)
            val results = imageClassifier?.classify(tensorImage) ?: return emptyList()

            results.flatMap { classifications ->
                classifications.categories.map { category ->
                    FoodResult(
                        label = category.label,
                        confidence = category.score
                    )
                }
            }.sortedByDescending { it.confidence }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Clean up classifier resources
    fun close() {
        imageClassifier?.close()
        imageClassifier = null
    }
}