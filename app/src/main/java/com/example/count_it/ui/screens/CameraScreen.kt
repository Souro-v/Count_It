package com.example.count_it.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.count_it.viewmodel.FoodViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    viewModel: FoodViewModel,
    onNavigateToResult: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Camera permission state
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    // ImageCapture use case
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    // Loading state while capturing
    var isCapturing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scan Food 📸",
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                cameraPermissionState.status.isGranted -> {
                    // Show camera preview
                    CameraPreview(
                        context = context,
                        lifecycleOwner = lifecycleOwner,
                        onImageCaptureReady = { capture ->
                            imageCapture = capture
                        }
                    )

                    // Overlay UI on camera
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Instruction card at top
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = "Point camera at your food",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
                            )
                        }

                        // Capture button at bottom
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (isCapturing) {
                                CircularProgressIndicator(color = Color.White)
                                Text(
                                    text = "Processing...",
                                    color = Color.White
                                )
                            } else {
                                // Shutter button
                                IconButton(
                                    onClick = {
                                        isCapturing = true
                                        captureImage(
                                            context = context,
                                            imageCapture = imageCapture,
                                            onImageCaptured = { bitmap ->
                                                viewModel.onImageCaptured(bitmap)
                                                isCapturing = false
                                                onNavigateToResult()
                                            },
                                            onError = {
                                                isCapturing = false
                                            }
                                        )
                                    },
                                    modifier = Modifier
                                        .size(72.dp)
                                        .border(
                                            width = 4.dp,
                                            color = Color.White,
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Camera,
                                        contentDescription = "Capture",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Text(
                                    text = "Tap to capture",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                cameraPermissionState.status.shouldShowRationale -> {
                    // Show rationale if permission was denied before
                    PermissionRationaleContent(
                        onRequestPermission = {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    )
                }

                else -> {
                    // First time - auto request permission
                    LaunchedEffect(Unit) {
                        cameraPermissionState.launchPermissionRequest()
                    }
                    PermissionRationaleContent(
                        onRequestPermission = {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    )
                }
            }
        }
    }
}

// CameraX preview composable
@Composable
fun CameraPreview(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Setup preview use case
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Setup image capture use case
            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            onImageCaptureReady(imageCapture)

            try {
                // Unbind before rebinding
                cameraProvider.unbindAll()

                // Bind to lifecycle
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}

// Capture photo and convert to Bitmap
private fun captureImage(
    context: Context,
    imageCapture: ImageCapture?,
    onImageCaptured: (Bitmap) -> Unit,
    onError: () -> Unit
) {
    if (imageCapture == null) {
        onError()
        return
    }

    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = imageProxyToBitmap(image)
                image.close()
                onImageCaptured(bitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
                onError()
            }
        }
    )
}

// Convert ImageProxy to Bitmap with correct rotation
private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    val matrix = Matrix()
    matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())

    return Bitmap.createBitmap(
        bitmap, 0, 0,
        bitmap.width, bitmap.height,
        matrix, true
    )
}

// UI shown when camera permission is not granted
@Composable
fun PermissionRationaleContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "📷", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Camera Permission Required",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We need camera access to scan your food and calculate calories.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text(text = "Grant Permission")
        }
    }
}