package ys.mobile.finoteapp.model

data class TransactionUiModel(
    val id: Int,
    val title: String,
    val date: String,
    val amountFormatted: String,
    val isIncome: Boolean,
    val iconRes: Int
)

