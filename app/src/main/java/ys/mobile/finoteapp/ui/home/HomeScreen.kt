package ys.mobile.finoteapp.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ys.mobile.finoteapp.DEMO_USER_ID
import ys.mobile.finoteapp.viewmodel.TransactionListUiState
import ys.mobile.finoteapp.viewmodel.TransactionListViewModel
import java.util.*

@Composable
fun StatCard(
    title: String,
    amount: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = amount,
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class HomeTransactionUI(
    val id: String,
    val title: String,
    val category: String,
    val amount: Long,
    val date: String,
    val isExpense: Boolean
)

@Composable
fun TransactionItemHome(transaction: HomeTransactionUI) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = transaction.category,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = transaction.date,
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }

            val formattedAmount = "%,d".format(transaction.amount).replace(",", ".")
            Text(
                text = if (transaction.isExpense) "-Rp $formattedAmount" else "+Rp $formattedAmount",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isExpense) Color.Red else Color.Green
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TransactionListViewModel,
    onAddTransactionClick: () -> Unit,
    onHistoryClick: () -> Unit = {}
) {
    // Ambil state dari ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val stats by viewModel.stats.collectAsState()

    // Load data ketika screen pertama kali tampil
    LaunchedEffect(Unit) {
        viewModel.getTransactions(DEMO_USER_ID)
    }

    val greeting = getGreeting()

    // Ambil statistik (default 0 jika belum ada)
    val totalBalance = stats["balance"] ?: 0L
    val totalIncome = stats["total_income"] ?: 0L
    val totalExpense = stats["total_expense"] ?: 0L

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Finote",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Text(
                            text = "Selamat $greeting 👋",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "Riwayat Transaksi",
                            tint = Color.Red
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = Color.Red
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Transaksi",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // --- STATISTIC CARDS ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                StatCard(
                    title = "Saldo",
                    amount = "Rp ${formatCurrency(totalBalance)}",
                    backgroundColor = Color(0xFF4CAF50)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Pemasukan",
                        amount = "Rp ${formatCurrency(totalIncome)}",
                        backgroundColor = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Pengeluaran",
                        amount = "Rp ${formatCurrency(totalExpense)}",
                        backgroundColor = Color(0xFFF44336),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Transaksi Terbaru",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // --- TRANSACTION LIST ---
            when (uiState) {
                is TransactionListUiState.Success -> {
                    val list = (uiState as TransactionListUiState.Success).transactions.take(5)

                    items(list) { t ->
                        TransactionItemHome(
                            HomeTransactionUI(
                                id = t.id,
                                title = t.title,
                                category = t.category,
                                amount = t.amount,
                                date = formatDisplayDate(t.date),
                                isExpense = !t.isIncome
                            )
                        )
                    }
                }

                TransactionListUiState.Loading -> {
                    item { CircularProgressIndicator() }
                }

                is TransactionListUiState.Error -> {
                    item {
                        Text(
                            text = "Gagal memuat data transaksi",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Pagi"
        in 12..16 -> "Siang"
        in 17..21 -> "Malam"
        else -> "Malam"
    }
}

fun formatCurrency(value: Long): String =
    "%,d".format(value).replace(",", ".")

fun formatDisplayDate(dateString: String): String {
    return try {
        val datePart = dateString.substringBefore("T")
        val parts = datePart.split("-")
        "${parts[2]} ${monthName(parts[1])} ${parts[0]}"
    } catch (_: Exception) {
        dateString
    }
}

fun monthName(m: String): String = when (m) {
    "01" -> "Jan"
    "02" -> "Feb"
    "03" -> "Mar"
    "04" -> "Apr"
    "05" -> "Mei"
    "06" -> "Jun"
    "07" -> "Jul"
    "08" -> "Agu"
    "09" -> "Sep"
    "10" -> "Okt"
    "11" -> "Nov"
    "12" -> "Des"
    else -> m
}
