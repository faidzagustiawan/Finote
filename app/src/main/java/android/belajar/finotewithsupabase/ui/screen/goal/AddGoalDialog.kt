package android.belajar.finotewithsupabase.ui.screen.goal

import android.belajar.finotewithsupabase.data.model.FinoteGoal
import android.belajar.finotewithsupabase.utils.DateUtils
import android.belajar.finotewithsupabase.ui.components.OutlinedTextFieldCustom
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

@Composable
fun AddGoalDialog(onDismiss: () -> Unit, existingGoal: FinoteGoal? = null, onConfirm: (FinoteGoal) -> Unit) {
    // Mapping existing data
    var goalName by remember { mutableStateOf(existingGoal?.title ?: "") }

    // Convert Double ke String untuk textfield.
    var targetAmount by remember {
        mutableStateOf(existingGoal?.targetAmount?.let {
            if(it % 1.0 == 0.0) it.toLong().toString() else it.toString()
        } ?: "")
    }

    var selectedImageUri by remember { mutableStateOf<Uri?>(existingGoal?.imageUri?.let { Uri.parse(it) }) }

    // Auto Deduct Fields
    var startDateMillis by remember { mutableStateOf(existingGoal?.startDateMillis) }
    var endDateMillis by remember { mutableStateOf(existingGoal?.endDateMillis) }
    var autoDeductFrequency by remember { mutableStateOf(existingGoal?.autoDeductFrequency ?: "Mati") }

    var autoDeductAmount by remember {
        mutableStateOf(existingGoal?.autoDeductAmount?.let {
            if (it > 0) {
                if(it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            } else ""
        } ?: "")
    }

    var selectedTime by remember { mutableStateOf(existingGoal?.autoDeductTime ?: "") }
    var selectedDay by remember { mutableStateOf(existingGoal?.autoDeductDay ?: "") }
    var selectedDate by remember { mutableStateOf(existingGoal?.autoDeductDate ?: "") }

    // UI States
    var showDateRangePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isFrequencyDropdownExpanded by remember { mutableStateOf(false) }
    var isDayDropdownExpanded by remember { mutableStateOf(false) }
    var isDateDropdownExpanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> selectedImageUri = uri }
    val dateRangeText = if (startDateMillis != null && endDateMillis != null) "${DateUtils.convertMillisToDate(startDateMillis)} - ${DateUtils.convertMillisToDate(endDateMillis)}" else ""
    val title = if (existingGoal != null) "Edit Goal" else "New Goal"
    val btnText = if (existingGoal != null) "UPDATE" else "SIMPAN"

    // Dialogs
    if (showDateRangePicker) DateRangePickerModal({ start, end -> startDateMillis = start; endDateMillis = end }, { showDateRangePicker = false })
    if (showTimePicker) TimePickerModal({ selectedTime = it }, { showTimePicker = false })

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize().background(Color.White), color = Color.White) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
                // Header
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Close, null, tint = Color.Gray) }
                    Text(title, fontSize = 28.sp)
                    Spacer(modifier = Modifier.size(40.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Image Picker
                Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color(0xFFD3D3D3)).align(Alignment.CenterHorizontally).clickable { imagePickerLauncher.launch("image/*") }, contentAlignment = Alignment.Center) {
                    if (selectedImageUri != null) AsyncImage(model = selectedImageUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Basic Info
                OutlinedTextFieldCustom(goalName, { goalName = it }, "Nama Goal")
                Spacer(modifier = Modifier.height(16.dp))

                // Input hanya angka (termasuk desimal jika mau)
                OutlinedTextFieldCustom(targetAmount, { if (it.all { c -> c.isDigit() || c == '.' }) targetAmount = it }, "Target Jumlah (Rp)")
                Spacer(modifier = Modifier.height(16.dp))

                Box {
                    OutlinedTextFieldCustom(dateRangeText, {}, "Pilih Rentang Tanggal", Icons.Default.DateRange, enabled = false)
                    Box(modifier = Modifier.matchParentSize().clickable { showDateRangePicker = true })
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Auto Deduct
                Text("Pengaturan Auto Potong", fontSize = 18.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE8E8E8)).clickable { isFrequencyDropdownExpanded = true }.padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(autoDeductFrequency, fontSize = 16.sp); Icon(Icons.Default.ArrowDropDown, null) }
                    DropdownMenu(expanded = isFrequencyDropdownExpanded, onDismissRequest = { isFrequencyDropdownExpanded = false }, modifier = Modifier.background(Color.White)) { listOf("Mati", "Harian", "Mingguan", "Bulanan").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { autoDeductFrequency = it; isFrequencyDropdownExpanded = false }) } }
                }

                if (autoDeductFrequency != "Mati") {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextFieldCustom(autoDeductAmount, { if (it.all { c -> c.isDigit() || c == '.' }) autoDeductAmount = it }, "Nominal per $autoDeductFrequency")
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextFieldCustom(selectedTime, {}, "Jam", Icons.Default.AccessTime, enabled = false)
                            Box(modifier = Modifier.matchParentSize().clickable { showTimePicker = true })
                        }
                        if (autoDeductFrequency == "Mingguan") { Box(modifier = Modifier.weight(1f).height(60.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE8E8E8)).clickable { isDayDropdownExpanded = true }.padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(if (selectedDay.isEmpty()) "Hari" else selectedDay); Icon(Icons.Default.ArrowDropDown, null) }; DropdownMenu(expanded = isDayDropdownExpanded, onDismissRequest = { isDayDropdownExpanded = false }, modifier = Modifier.background(Color.White)) { listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { selectedDay = it; isDayDropdownExpanded = false }) } } } }
                        if (autoDeductFrequency == "Bulanan") { Box(modifier = Modifier.weight(1f).height(60.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE8E8E8)).clickable { isDateDropdownExpanded = true }.padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(if (selectedDate.isEmpty()) "Tgl" else selectedDate); Icon(Icons.Default.ArrowDropDown, null) }; DropdownMenu(expanded = isDateDropdownExpanded, onDismissRequest = { isDateDropdownExpanded = false }, modifier = Modifier.background(Color.White)) { (1..31).map { it.toString() }.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { selectedDate = it; isDateDropdownExpanded = false }) } } } }
                    }
                }
                Spacer(modifier = Modifier.weight(1f)); Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (goalName.isNotBlank() && targetAmount.isNotBlank()) {
                            onConfirm(FinoteGoal(
                                id = existingGoal?.id ?: "",
                                // === PERBAIKAN UTAMA ===
                                // Pastikan userId tidak hilang saat Update
                                userId = existingGoal?.userId ?: "",
                                // =======================
                                title = goalName,
                                targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
                                currentAmount = existingGoal?.currentAmount ?: 0.0,
                                startDateMillis = startDateMillis,
                                endDateMillis = endDateMillis,
                                autoDeductFrequency = autoDeductFrequency,
                                autoDeductAmount = autoDeductAmount.toDoubleOrNull() ?: 0.0,
                                autoDeductTime = selectedTime,
                                autoDeductDay = selectedDay,
                                autoDeductDate = selectedDate,
                                imageUri = selectedImageUri?.toString(),
                                autoDeduct = autoDeductFrequency != "Mati"
                            ))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(btnText, fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}