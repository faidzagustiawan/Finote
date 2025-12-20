package ys.mobile.finoteapp.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import io.github.jan.supabase.auth.auth
import ys.mobile.finoteapp.data.remote.SupabaseClient
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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

@Composable
fun WalletCard(
    totalBalance: Long,
    income: Long,
    expense: Long,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black), // Premium Black
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Saldo",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Rp ${formatCurrency(totalBalance)}",
                fontSize = 32.sp, // Bigger & Bolder
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = Color.DarkGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income
                Column(horizontalAlignment = Alignment.Start) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFF1E3A20), androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.ArrowDownward, null, tint = Color.Green, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pemasukan", fontSize = 12.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rp ${formatCurrency(income)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Expense
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Pengeluaran", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFF3E1F1F), androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.ArrowUpward, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rp ${formatCurrency(expense)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
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
    val currentUser = remember { SupabaseClient.client.auth.currentUserOrNull() }
    val mainBalance by viewModel.mainBalance.collectAsState()
    // Load data menggunakan ID user asli
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            viewModel.getTransactions(user.id)
            viewModel.fetchMainBalance(user.id)
        }
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
                            text = "FINOTE", // Matching Goal/History Style
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = Color.Black
                        )
                        val userName = currentUser?.userMetadata?.get("Display name")?.toString()
                            ?: currentUser?.email?.substringBefore("@")
                            ?: "User"
                        Text(
                            text = "Selamat $greeting, $userName 👋",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "Riwayat Transaksi",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = Color.Red, // Updated to Red as requested
                contentColor = Color.White,
                shape = androidx.compose.foundation.shape.CircleShape
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- WALLET CARD ---
            item {
                WalletCard(
                    totalBalance = mainBalance,
                    income = stats["total_income"] ?: 0L,
                    expense = stats["total_expense"] ?: 0L
                )
            }

            item {
                Text(
                    text = "TRANSAKSI TERBARU", // Matching Typography
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
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

            item { Spacer(modifier = Modifier.height(80.dp)) }
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
