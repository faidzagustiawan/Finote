package android.belajar.finotewithsupabase.ui.screen.goal

import android.belajar.finotewithsupabase.data.model.TransactionType
import android.belajar.finotewithsupabase.utils.DateUtils
import android.belajar.finotewithsupabase.ui.components.GrayInputBox
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun TransactionDialog(
    type: TransactionType,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long, String, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var dateString by remember { mutableStateOf(DateUtils.convertMillisToDate(System.currentTimeMillis())) }
    var timeString by remember { mutableStateOf("12:00") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val title = if (type == TransactionType.DEPOSIT) "Deposit" else "Withdraw"

    if (showDatePicker) DatePickerModal({ if (it != null) { dateMillis = it; dateString = DateUtils.convertMillisToDate(it) } }, { showDatePicker = false })
    if (showTimePicker) TimePickerModal({ timeString = it }, { showTimePicker = false })

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) { Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE0E0E0))) }; Spacer(modifier = Modifier.weight(1f)); Text(title, fontSize = 24.sp, fontWeight = FontWeight.Normal); Spacer(modifier = Modifier.weight(1f)); Spacer(modifier = Modifier.width(40.dp))
                }
                Spacer(modifier = Modifier.height(40.dp))

                GrayInputBox(amount, { if (it.all { c -> c.isDigit() }) amount = it }, "Jumlah"); Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) { GrayInputBox(dateString, {}, "Tanggal", true); Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true }) };
                    Box(modifier = Modifier.weight(1f)) { GrayInputBox(timeString, {}, "Jam", true); Box(modifier = Modifier.matchParentSize().clickable { showTimePicker = true }) }
                }
                Spacer(modifier = Modifier.height(16.dp))

                TextField(value = note, onValueChange = { note = it }, placeholder = { Text("Note", color = Color.Gray) }, modifier = Modifier.fillMaxWidth().height(150.dp), colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFE0E0E0), unfocusedContainerColor = Color(0xFFE0E0E0), disabledContainerColor = Color(0xFFE0E0E0), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent), shape = RoundedCornerShape(12.dp))

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { if (amount.isNotEmpty()) onConfirm(amount.toLong(), dateMillis, timeString, note) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save", fontSize = 18.sp, color = Color.Black)
                }
            }
        }
    }
}