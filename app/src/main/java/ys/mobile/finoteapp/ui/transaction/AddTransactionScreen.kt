package ys.mobile.finoteapp.ui.transaction

import ys.mobile.finoteapp.DEMO_USER_ID
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ys.mobile.finoteapp.model.Transaction
import ys.mobile.finoteapp.viewmodel.TransactionListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    onScanReceipt: () -> Unit,
    viewModel: TransactionListViewModel = viewModel(),
    navController: NavController? = null
) {
    val context = LocalContext.current
    var nominal by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(getCurrentDate()) }
    var isLoading by remember { mutableStateOf(false) }
    var isIncome by remember { mutableStateOf(false) }

    // Listen for OCR result
    if (navController != null) {
        val currentBackStackEntry = navController.currentBackStackEntry
        val savedStateHandle = currentBackStackEntry?.savedStateHandle
        
        // Use LaunchedEffect with side-effect to poll valid data
        // Alternatively, use collectAsState if we had the dep, but simplistic standard approach:
        val ocrAmount = savedStateHandle?.get<Long>("ocr_amount")
        val ocrIsIncome = savedStateHandle?.get<Boolean>("ocr_is_income")
        
        LaunchedEffect(ocrAmount, ocrIsIncome) {
            if (ocrAmount != null) {
                nominal = ocrAmount.toString()
                isIncome = ocrIsIncome ?: false
                
                // Clear state so it doesn't re-apply if we navigate back and forth without new scan
                savedStateHandle.remove<Long>("ocr_amount")
                savedStateHandle.remove<Boolean>("ocr_is_income")
                
                Toast.makeText(context, "Data struk berhasil dimuat!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Tambah Transaksi", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        // Nominal
        TextField(
            value = nominal,
            onValueChange = { nominal = it },
            label = { Text("Nominal (Rp)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Kategori
        TextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Kategori") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Catatan
        TextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Catatan (Opsional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Tanggal
        TextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Tanggal (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Tipe Transaksi (radio buttons atau checkbox)
        Text("Tipe: ${if (isIncome) "Pemasukan" else "Pengeluaran"}")
        Button(onClick = { isIncome = !isIncome }) {
            Text("Ubah ke ${if (isIncome) "Pengeluaran" else "Pemasukan"}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onScanReceipt) {
            Text(text = "Scan Struk")
        }
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                // Validasi input
                if (nominal.isEmpty() || category.isEmpty() || date.isEmpty()) {
                    Toast.makeText(context, "Isi semua field yang diperlukan", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val amount = nominal.toLongOrNull()
                if (amount == null || amount <= 0) {
                    Toast.makeText(context, "Nominal harus angka positif", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true

                // Buat transaction object
                val transaction = Transaction(
                    userId = DEMO_USER_ID, // Gunakan demo_user untuk sekarang
                    title = category,
                    amount = amount,
                    isIncome = isIncome,
                    category = category,
                    date = "${date}T${getCurrentTime()}",
                    notes = if (note.isEmpty()) null else note
                )

                // Simpan ke database menggunakan ViewModel
                viewModel.addTransaction(transaction, DEMO_USER_ID)

                isLoading = false
                Toast.makeText(context, "Transaksi berhasil disimpan!", Toast.LENGTH_SHORT).show()
                onBack()
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text(text = "Simpan")
            }
        }
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
