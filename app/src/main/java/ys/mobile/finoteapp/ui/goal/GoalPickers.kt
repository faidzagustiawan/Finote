package ys.mobile.finoteapp.ui.goal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(onDateSelected: (Long?) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onDateSelected(datePickerState.selectedDateMillis); onDismiss() }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    ) { DatePicker(state = datePickerState) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(onDateRangeSelected: (Long?, Long?) -> Unit, onDismiss: () -> Unit) {
    val dateRangePickerState = rememberDateRangePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onDateRangeSelected(dateRangePickerState.selectedStartDateMillis, dateRangePickerState.selectedEndDateMillis); onDismiss() }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    ) { DateRangePicker(state = dateRangePickerState, title = { Text("Pilih Rentang", modifier = Modifier.padding(16.dp)) }, showModeToggle = false, modifier = Modifier.weight(1f)) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerModal(onTimeSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(initialHour = currentTime.get(Calendar.HOUR_OF_DAY), initialMinute = currentTime.get(Calendar.MINUTE), is24Hour = true)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { val hour = timePickerState.hour.toString().padStart(2, '0'); val minute = timePickerState.minute.toString().padStart(2, '0'); onTimeSelected("$hour:$minute"); onDismiss() }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    ) { Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("Pilih Jam", modifier = Modifier.padding(bottom = 20.dp), fontWeight = FontWeight.Bold); TimePicker(state = timePickerState) } }
}
