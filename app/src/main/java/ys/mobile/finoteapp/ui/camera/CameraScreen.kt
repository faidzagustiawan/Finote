package ys.mobile.finoteapp.ui.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    onResult: (Long, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                Toast.makeText(context, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
                onBack()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        CameraContent(onResult = onResult, onBack = onBack)
    }
}

@Composable
fun CameraContent(
    onResult: (Long, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Camera X
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var camera: Camera? by remember { mutableStateOf(null) }
    var flashEnabled by remember { mutableStateOf(false) }
    
    // UI State
    var isProcessing by remember { mutableStateOf(false) }
    var detectedNumbers by remember { mutableStateOf<List<Long>>(emptyList()) }
    var showResultDialog by remember { mutableStateOf(false) }
    
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val textRecognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!showResultDialog) {
            // CAMERA PREVIEW
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        
                        val preview = Preview.Builder().build()
                        preview.setSurfaceProvider(previewView.surfaceProvider)

                        imageCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            Log.e("CameraScreen", "Use case binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // OVERLAY UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            flashEnabled = !flashEnabled
                            camera?.cameraControl?.enableTorch(flashEnabled)
                        },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = Color.White
                        )
                    }
                }

                // Capture Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            if (!isProcessing) {
                                isProcessing = true
                                captureAndProcessImage(
                                    context,
                                    imageCapture,
                                    cameraExecutor,
                                    textRecognizer,
                                    onSuccess = { numbers ->
                                        isProcessing = false
                                        detectedNumbers = numbers
                                        showResultDialog = true
                                    },
                                    onError = {
                                        isProcessing = false
                                        Toast.makeText(context, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier.size(80.dp).border(4.dp, Color.Gray, CircleShape),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(40.dp), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        } else {
            // RESULT SELECTION UI
            ResultSelectionScreen(
                numbers = detectedNumbers,
                onRetake = { showResultDialog = false },
                onConfirm = { amount, isIncome ->
                   onResult(amount, isIncome)
                }
            )
        }
    }
}

@Composable
fun ResultSelectionScreen(
    numbers: List<Long>,
    onRetake: () -> Unit,
    onConfirm: (Long, Boolean) -> Unit
) {
    var selectedAmount by remember { mutableStateOf(if (numbers.isNotEmpty()) numbers[0] else 0L) }
    var isIncome by remember { mutableStateOf(false) } // Default Expense
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onRetake) {
                Icon(Icons.Default.ArrowBack, "Retake")
            }
            Text("Hasil Scan", style = MaterialTheme.typography.titleLarge)
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Selected Value
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)) {
                Text("Nomor Terpilih", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "Rp ${formatReference(selectedAmount)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Type Selection
        Text("Tipe Transaksi", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FilterChip(
                selected = !isIncome,
                onClick = { isIncome = false },
                label = { Text("Pengeluaran") },
                leadingIcon = { if (!isIncome) Icon(Icons.Default.Check, null) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.Red.copy(alpha = 0.2f),
                    selectedLabelColor = Color.Red
                ),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = isIncome,
                onClick = { isIncome = true },
                label = { Text("Pemasukan") },
                leadingIcon = { if (isIncome) Icon(Icons.Default.Check, null) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.Green.copy(alpha = 0.2f),
                    selectedLabelColor = Color.Green
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Angka Terdeteksi Lainnya", style = MaterialTheme.typography.titleMedium)
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(numbers) { number ->
                Card(
                    onClick = { selectedAmount = number },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedAmount == number) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Rp ${formatReference(number)}",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            if (numbers.isEmpty()) {
                item {
                    Text("Tidak ada angka yang terdeteksi.", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }
        }
        
        Button(
            onClick = { onConfirm(selectedAmount, isIncome) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Gunakan Angka Ini")
        }
    }
}

private fun captureAndProcessImage(
    context: Context,
    imageCapture: ImageCapture?,
    executor: ExecutorService,
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    onSuccess: (List<Long>) -> Unit,
    onError: (Exception) -> Unit
) {
    if (imageCapture == null) return

    imageCapture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
                val mediaImage = image.image
                if (mediaImage != null) {
                    val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
                    
                    recognizer.process(inputImage)
                        .addOnSuccessListener { visionText ->
                            val numbers = extractNumbers(visionText.text)
                            launchMain { onSuccess(numbers) }
                            image.close()
                        }
                        .addOnFailureListener { e ->
                            launchMain { onError(e) }
                            image.close()
                        }
                } else {
                    image.close()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                launchMain { onError(exception) }
            }
        }
    )
}

private fun launchMain(block: () -> Unit) {
    android.os.Handler(android.os.Looper.getMainLooper()).post(block)
}

// Regex to find numbers. Simple implementation.
private fun extractNumbers(text: String): List<Long> {
    // Regex matches numbers with various separators: 100, 100.000, 100,000, 8.000,00
    val regex = Regex("[0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]+)?")
    
    return regex.findAll(text)
        .mapNotNull { result ->
            var raw = result.value
            
            // Fix for Indonesian format (e.g. "8.000,00" -> 8000)
            // If comma is used as decimal separator (patterns like ",00", ",50", ",5")
            if (raw.contains(",")) {
                val parts = raw.split(",")
                val lastPart = parts.last()
                
                // If the part after the last comma is 1 or 2 digits, it's likely a decimal.
                // Examples: "8.000,00" -> strip ",00"
                // "12,5" -> strip ",5"
                // But "100,000" (US format) -> "000" (3 digits) -> Keep it.
                if (lastPart.length in 1..2) {
                    raw = raw.substringBeforeLast(",")
                }
            }
            
            // Remove all dots and remaining commas to get pure digits
            val clean = raw.replace(".", "").replace(",", "")
            
            // Filter noise: empty strings or too long sequences (though regex limits this somewhat)
            if (clean.isEmpty() || clean.length > 15) return@mapNotNull null
            
            clean.toLongOrNull()
        }
        .filter { it > 100 } // Filter out small noise
        .sortedDescending() // Largest likely total
        .distinct()
        .toList()
}

private fun formatReference(amount: Long): String {
    return String.format(java.util.Locale.US, "%,d", amount)
}
