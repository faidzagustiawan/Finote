package android.belajar.finotewithsupabase.ui.screen.goal

import android.belajar.finotewithsupabase.data.model.FinoteGoal
import android.belajar.finotewithsupabase.data.model.GoalTransaction
import android.belajar.finotewithsupabase.data.model.TransactionType
import android.belajar.finotewithsupabase.data.repository.GoalRepository
import android.belajar.finotewithsupabase.utils.CurrencyUtils
import android.belajar.finotewithsupabase.utils.DateUtils
import android.belajar.finotewithsupabase.ui.components.CircularDonutChart
import android.belajar.finotewithsupabase.ui.components.DetailInfoBox
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: String,
    onBack: () -> Unit
) {
    val goal = GoalRepository.goals.find { it.id == goalId }
    val transactions = GoalRepository.currentTransactions

    // Fetch Transaksi saat masuk detail
    LaunchedEffect(goalId) {
        GoalRepository.fetchTransactions(goalId)
    }

    if (goal == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Progress, 1 = Records
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var transactionType by remember { mutableStateOf(TransactionType.DEPOSIT) }

    if (showEditDialog) {
        AddGoalDialog(
            onDismiss = { showEditDialog = false },
            existingGoal = goal,
            onConfirm = { updatedGoal ->
                GoalRepository.updateGoal(updatedGoal)
                showEditDialog = false
            }
        )
    }

    if (showTransactionDialog) {
        TransactionDialog(
            type = transactionType,
            onDismiss = { showTransactionDialog = false },
            onConfirm = { amount, date, time, note ->
                if (transactionType == TransactionType.DEPOSIT) {
                    GoalRepository.deposit(goal.id, amount.toDouble())
                } else {
                    GoalRepository.withdraw(goal.id, amount.toDouble())
                }
                showTransactionDialog = false
            }
        )
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth().clickable { showBottomSheet = false; showEditDialog = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Edit Goal", fontSize = 18.sp)
                }

                Row(modifier = Modifier.fillMaxWidth().clickable { showBottomSheet = false; GoalRepository.deleteGoal(goal.id); onBack() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Delete Goal", fontSize = 18.sp, color = Color.Red)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = goal.title, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFEEEEEE)), contentAlignment = Alignment.Center) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                    }
                },
                actions = {
                    IconButton(onClick = { showBottomSheet = true }) {
                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFEEEEEE)), contentAlignment = Alignment.Center) { Icon(Icons.Default.MoreVert, contentDescription = "More") }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Tabs
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(120.dp).clickable { selectedTab = 0 }) {
                    Text("PROGRESS", fontWeight = if(selectedTab==0) FontWeight.Bold else FontWeight.Normal, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    if(selectedTab == 0) Box(modifier = Modifier.height(2.dp).fillMaxWidth().background(Color.Black))
                }
                Spacer(modifier = Modifier.width(32.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(120.dp).clickable { selectedTab = 1 }) {
                    Text("RECORDS", fontWeight = if(selectedTab==1) FontWeight.Bold else FontWeight.Normal, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    if(selectedTab == 1) Box(modifier = Modifier.height(2.dp).fillMaxWidth().background(Color.Black))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (selectedTab == 0) ProgressContent(goal)
                else RecordsContent(transactions)
            }

            Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { transactionType = TransactionType.DEPOSIT; showTransactionDialog = true }, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black), shape = RoundedCornerShape(12.dp)) { Text("Deposit", fontSize = 18.sp, color = Color.White) }
                Button(onClick = { transactionType = TransactionType.WITHDRAW; showTransactionDialog = true }, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)), shape = RoundedCornerShape(12.dp)) { Text("Withdraw", fontSize = 18.sp, color = Color.Black) }
            }
        }
    }
}

@Composable
fun ProgressContent(goal: FinoteGoal) {
    val greenColor = Color(0xFF00C853)
    val grayBoxColor = Color(0xFFE0E0E0)
    val trackColor = Color(0xFF757575)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(20.dp))
        Box(contentAlignment = Alignment.Center) {
            CircularDonutChart(goal.progress, 90.dp, 20.dp, greenColor, trackColor)
            Text("${(goal.progress * 100).toInt()} %", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(40.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailInfoBox("SAVED", CurrencyUtils.toRupiah(goal.currentAmount), Modifier.weight(1f), grayBoxColor)
                DetailInfoBox("TOTAL", CurrencyUtils.toRupiah(goal.targetAmount), Modifier.weight(1f), grayBoxColor)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailInfoBox("KURANG", "- " + CurrencyUtils.toRupiah(goal.remainingAmount), Modifier.weight(1f), grayBoxColor)
                DetailInfoBox("REMAINING", "${goal.remainingDays} Days Left", Modifier.weight(1f), grayBoxColor)
            }
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                DetailInfoBox("TARGET DATE", DateUtils.convertMillisToDate(goal.endDateMillis), Modifier.width(200.dp), grayBoxColor)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun RecordsContent(transactions: List<GoalTransaction>) {
    // Grouping logic based on Date
    // Note: Perlu penyesuaian karena field 'createdAt' di Supabase berupa String ISO

    if (transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Belum ada riwayat transaksi", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(transactions) { transaction ->
                RecordItem(transaction)
            }
        }
    }
}

@Composable
fun RecordItem(t: GoalTransaction) {
    val isDep = t.type == "deposit"
    val color = if (isDep) Color(0xFF00C853) else Color(0xFFFF5252)
    val icon = if (isDep) Icons.Default.Add else Icons.Default.Remove

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFEEEEEE)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(if(isDep) "Deposit" else "Withdraw", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Text(DateUtils.formatTransactionDate(t.createdAt), fontSize = 13.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Text(text = CurrencyUtils.toRupiah(t.amount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}