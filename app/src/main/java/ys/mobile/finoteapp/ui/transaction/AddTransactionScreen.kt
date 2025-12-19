package ys.mobile.finoteapp.ui.transaction

import ys.mobile.finoteapp.DEMO_USER_ID
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ys.mobile.finoteapp.model.Transaction
import ys.mobile.finoteapp.viewmodel.TransactionListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    onScanReceipt: () -> Unit,
    viewModel: TransactionListViewModel = viewModel(),
    navController: NavController? = null,
    transactionId: String? = null
) {
    val context = LocalContext.current
    
    // State
    var nominal by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(getCurrentDate()) } // YYYY-MM-DD
    var isIncome by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Date Picker State
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Load Existing Data for Edit Mode
    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            val existing = viewModel.getTransactionById(transactionId)
            if (existing != null) {
                nominal = existing.amount.toString()
                category = existing.category
                note = existing.notes ?: ""
                isIncome = existing.isIncome
                try {
                    date = existing.date.substringBefore("T")
                    // Convert YYYY-MM-DD back to millis for DatePicker if needed (optional)
                } catch (_: Exception) {
                    date = getCurrentDate()
                }
            } else {
                Toast.makeText(context, "Transaksi tidak ditemukan!", Toast.LENGTH_SHORT).show()
                onBack()
            }
        }
    }

    // OCR Result Listener
    if (navController != null) {
        val currentBackStackEntry = navController.currentBackStackEntry
        val savedStateHandle = currentBackStackEntry?.savedStateHandle
        
        val ocrAmount = savedStateHandle?.get<Long>("ocr_amount")
        val ocrIsIncome = savedStateHandle?.get<Boolean>("ocr_is_income")
        
        LaunchedEffect(ocrAmount, ocrIsIncome) {
            if (ocrAmount != null) {
                nominal = ocrAmount.toString()
                isIncome = ocrIsIncome ?: false
                savedStateHandle.remove<Long>("ocr_amount")
                savedStateHandle.remove<Boolean>("ocr_is_income")
                Toast.makeText(context, "Data struk berhasil dimuat!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val isEditMode = transactionId != null

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = convertMillisToDate(millis)
                    }
                    showDatePicker = false
                }) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEditMode) "Edit Transaksi" else "Tambah Transaksi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                   // Optional: Add Back Icon here if needed, but usually handled by system back or parent
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Transaction Type Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabButton(
                    text = "Pengeluaran",
                    isSelected = !isIncome,
                    selectedColor = Color(0xFFE53935), // Red
                    modifier = Modifier.weight(1f)
                ) { isIncome = false }
                
                TabButton(
                    text = "Pemasukan",
                    isSelected = isIncome,
                    selectedColor = Color(0xFF43A047), // Green
                    modifier = Modifier.weight(1f)
                ) { isIncome = true }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Nominal Input
            OutlinedTextField(
                value = nominal,
                onValueChange = { if (it.all { char -> char.isDigit() }) nominal = it },
                label = { Text("Nominal (Rp)") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Category Selection (Chips)
            Text("Kategori", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
            
            val expenseCategories = listOf("Makanan", "Transport", "Belanja", "Tagihan", "Hiburan", "Lainnya")
            val incomeCategories = listOf("Gaji", "Bonus", "Penjualan", "Investasi", "Lainnya")
            val currentCategories = if (isIncome) incomeCategories else expenseCategories

            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentCategories.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) },
                        leadingIcon = if (category == cat) {
                            { Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Date Picker Input (ReadOnly)
            OutlinedTextField(
                value = formatDateDisplay(date),
                onValueChange = {},
                readOnly = true,
                label = { Text("Tanggal") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }, // Make the whole field clickable
                enabled = false, // Disable typing, but handle click on parent box or interaction source
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            // Fix clickability for wrapper since enabled=false blocks click
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp) // Match TextField height
                    .offset(y = (-56).dp) // Overlay on top
                    .clickable { showDatePicker = true }
            )

            // 5. Notes Input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Catatan (Opsional)") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 6. Action Buttons
            if (!isEditMode) {
                OutlinedButton(
                    onClick = onScanReceipt,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan Struk (OCR)")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    if (nominal.isEmpty() || category.isEmpty()) {
                        Toast.makeText(context, "Nominal dan Kategori wajib diisi", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isLoading = true
                    val amountVal = nominal.toLongOrNull() ?: 0L
                    
                    val finalTransaction = Transaction(
                        id = transactionId ?: java.util.UUID.randomUUID().toString(),
                        userId = DEMO_USER_ID,
                        title = category,
                        amount = amountVal,
                        isIncome = isIncome,
                        category = category,
                        date = "${date}T${getCurrentTime()}",
                        notes = if (note.isEmpty()) null else note
                    )

                    if (isEditMode) {
                         viewModel.updateTransaction(finalTransaction, DEMO_USER_ID)
                         Toast.makeText(context, "Berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    } else {
                         viewModel.addTransaction(finalTransaction, DEMO_USER_ID)
                         Toast.makeText(context, "Berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    }
                    isLoading = false
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isEditMode) "Update Transaksi" else "Simpan Transaksi")
                }
            }
            
            if (isEditMode) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = {
                         if (!isLoading) {
                             isLoading = true
                             viewModel.deleteTransaction(transactionId!!, DEMO_USER_ID)
                             Toast.makeText(context, "Transaksi dihapus!", Toast.LENGTH_SHORT).show()
                             isLoading = false
                             onBack()
                         }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                     Text("Hapus Transaksi")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(4.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) selectedColor else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

private fun getCurrentDate(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date())
}

private fun getCurrentTime(): String {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(Date())
}

private fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(millis))
}

private fun formatDateDisplay(dateString: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
        SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(date!!) // e.g., 19 Desember 2025
    } catch (e: Exception) {
        dateString
    }
}
