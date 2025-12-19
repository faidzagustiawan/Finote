package ys.mobile.finoteapp.ui.transaction

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ys.mobile.finoteapp.R
import ys.mobile.finoteapp.model.TransactionUiModel

@Composable
fun TransactionRow(
    item: TransactionUiModel,
    onClick: (TransactionUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(item) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Icon kategori di kiri
            Image(
                imageVector = item.icon,
                contentDescription = "${item.title} icon",
                modifier = Modifier.size(48.dp)
            )

            // Title & Date di tengah
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = item.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            // Amount di kanan dengan warna berbeda
            val amountColor = if (item.isIncome) {
                Color(0xFF2E7D32) // Hijau untuk pemasukan
            } else {
                Color(0xFFD32F2F) // Merah untuk pengeluaran
            }

            Text(
                text = item.amountFormatted,
                style = MaterialTheme.typography.bodyMedium,
                color = amountColor,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TransactionRowPreview() {
    val sampleIncome = TransactionUiModel(
        id = "1",
        title = "Gaji November",
        date = "30 Nov 2025",
        amountFormatted = "+Rp 5.000.000",
        isIncome = true,
        icon = Icons.Filled.AttachMoney
    )
    TransactionRow(item = sampleIncome, onClick = {})
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TransactionRowPreviewExpense() {
    val sampleExpense = TransactionUiModel(
        id = "2",
        title = "Belanja Bulanan",
        date = "30 Nov 2025",
        amountFormatted = "-Rp 250.000",
        isIncome = false,
        icon = Icons.Filled.ShoppingCart
    )
    TransactionRow(item = sampleExpense, onClick = {})
}

