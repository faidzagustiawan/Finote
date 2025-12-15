package android.belajar.finotewithsupabase.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun OutlinedTextFieldCustom(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        trailingIcon = if (trailingIcon != null) { { Icon(trailingIcon, null, tint = Color.Gray) } } else null,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE8E8E8),
            unfocusedContainerColor = Color(0xFFE8E8E8),
            disabledContainerColor = Color(0xFFE8E8E8),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            disabledTextColor = Color.Black
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        enabled = enabled
    )
}

@Composable
fun GrayInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isReadOnly: Boolean = false, // Parameter untuk mode "hanya baca" (misal: DatePicker)
    enabled: Boolean = true
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Black) },
        modifier = Modifier.fillMaxWidth().height(60.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE0E0E0),
            unfocusedContainerColor = Color(0xFFE0E0E0),
            disabledContainerColor = Color(0xFFE0E0E0),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            disabledTextColor = Color.Black
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        enabled = enabled,
        readOnly = isReadOnly,
        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black)
    )
}