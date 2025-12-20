package ys.mobile.finoteapp.model

import androidx.compose.ui.graphics.vector.ImageVector

data class TransactionUiModel(
    val id: String,
    val title: String,
    val date: String,
    val amountFormatted: String,
    val isIncome: Boolean,
    val icon: ImageVector
)

