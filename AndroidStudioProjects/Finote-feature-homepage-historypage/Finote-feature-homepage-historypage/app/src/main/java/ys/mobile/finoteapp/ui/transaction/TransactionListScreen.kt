package ys.mobile.finoteapp.ui.transaction

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jan.supabase.auth.auth
import ys.mobile.finoteapp.data.remote.SupabaseClient
import ys.mobile.finoteapp.viewmodel.TransactionListViewModel
import ys.mobile.finoteapp.viewmodel.TransactionListUiState
import ys.mobile.finoteapp.model.TransactionUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    viewModel: TransactionListViewModel = viewModel(),
    onBack: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = remember { SupabaseClient.client.auth.currentUserOrNull() }
    // Gunakan rememberSaveable dengan LazyListState.Saver untuk menjaga posisi scroll
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    // Load data saat screen dibuka (hanya sekali)
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            viewModel.getTransactions(user.id)
        } ?: run {
           onBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "RIWAYAT TRANSAKSI",
                        fontSize = 24.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Tampilkan content berdasarkan UiState
            when (val state = uiState) {
                is TransactionListUiState.Loading -> {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
                is TransactionListUiState.Success -> {
                    val transactions = state.transactions
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = transactions,
                            key = { it.id }
                        ) { transaction ->
                            val uiModel = viewModel.mapToUiModel(transaction)
                            TransactionRow(
                                item = uiModel,
                                onClick = { clicked ->
                                     // Navigate to Edit
                                     onNavigateToEdit(clicked.id)
                                }
                            )
                        }
                    }
                }
                is TransactionListUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}



