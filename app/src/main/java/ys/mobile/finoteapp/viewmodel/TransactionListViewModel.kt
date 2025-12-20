package ys.mobile.finoteapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ys.mobile.finoteapp.data.repository.TransactionRepository
import ys.mobile.finoteapp.model.Transaction
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

sealed class TransactionListUiState {
    data object Loading : TransactionListUiState()
    data class Success(val transactions: List<Transaction>) : TransactionListUiState()
    data class Error(val message: String) : TransactionListUiState()
}

class TransactionListViewModel(
    private val repository: TransactionRepository = TransactionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransactionListUiState>(TransactionListUiState.Loading)
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    private val _stats = MutableStateFlow<Map<String, Long>>(emptyMap())
    val stats: StateFlow<Map<String, Long>> = _stats.asStateFlow()

    init {
        viewModelScope.launch {
            ys.mobile.finoteapp.utils.GsonUtils.generateTransactionJson()
        }
    }

    /**
     * Mengambil daftar transaksi untuk user
     */
    fun getTransactions(userId: String) {

        viewModelScope.launch {
            _uiState.update { TransactionListUiState.Loading }

            try {
                Log.d("SUPABASE_DEBUG", "ViewModel: calling getTransactions()")
                val data = repository.getTransactions(userId)

                // Use server data if available. Only fallback to dummy when exception occurs.
                _uiState.update { TransactionListUiState.Success(data) }
                loadStatsFromList(data)
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "Error in getTransactions(): ${e.message}")

                // If exception (network/server), fallback to dummy data to keep UI functional
                val fallback = createDummyTransactions(userId)
                _uiState.update { TransactionListUiState.Success(fallback) }
                loadStatsFromList(fallback)
            }
        }
    }


    /**
     * Menambahkan transaksi baru
     */
    fun addTransaction(transaction: Transaction, userId: String) {
        viewModelScope.launch {
            try {
                val newTransaction = transaction.copy(userId = userId)
                repository.addTransaction(newTransaction)
                // Refresh list setelah menambahkan
                getTransactions(userId)
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "Failed to addTransaction(): ${e.message}")
                // Jika gagal menyimpan ke remote, tambahkan ke local list (optimistic)
                val current = (uiState.value as? TransactionListUiState.Success)?.transactions?.toMutableList() ?: createDummyTransactions(userId).toMutableList()
                current.add(0, transaction)
                _uiState.update { TransactionListUiState.Success(current) }
                loadStatsFromList(current)
            }
        }
    }

    /**
     * Menghapus transaksi
     */
    fun deleteTransaction(transactionId: String, userId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transactionId)
                // Refresh list setelah menghapus
                getTransactions(userId)
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "Failed to deleteTransaction(): ${e.message}")
                // Jika gagal remote, remove locally
                val current = (uiState.value as? TransactionListUiState.Success)?.transactions?.filter { it.id != transactionId } ?: createDummyTransactions(userId)
                _uiState.update { TransactionListUiState.Success(current) }
                loadStatsFromList(current)
            }
        }
    }

    /**
     * Memperbarui transaksi
     */
    fun updateTransaction(transaction: Transaction, userId: String) {
        viewModelScope.launch {
            try {
                repository.updateTransaction(transaction)
                // Refresh list setelah memperbarui
                getTransactions(userId)
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "Failed to updateTransaction(): ${e.message}")
                // Jika gagal remote, update locally
                val current = (uiState.value as? TransactionListUiState.Success)?.transactions?.map {
                    if (it.id == transaction.id) transaction else it
                } ?: createDummyTransactions(userId)
                _uiState.update { TransactionListUiState.Success(current) }
                loadStatsFromList(current)
            }
        }
    }

    /**
     * Mengambil transaksi berdasarkan kategori
     */
    fun getTransactionsByCategory(userId: String, category: String) {
        viewModelScope.launch {
            _uiState.update { TransactionListUiState.Loading }
            try {
                val transactions = repository.getTransactionsByCategory(userId, category)
                _uiState.update { TransactionListUiState.Success(transactions) }
                loadStatsFromList(transactions)
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "Failed to getTransactionsByCategory(): ${e.message}")
                // fallback empty
                _uiState.update { TransactionListUiState.Success(emptyList()) }
                loadStatsFromList(emptyList())
            }
        }
    }

    /**
     * Mengambil satu transaksi berdasarkan ID dari list yang sudah ada
     */
    fun getTransactionById(id: String): Transaction? {
        return (uiState.value as? TransactionListUiState.Success)?.transactions?.find { it.id == id }
    }

    // helper to map domain to UI model
    fun mapToUiModel(transaction: Transaction): ys.mobile.finoteapp.model.TransactionUiModel {
        return ys.mobile.finoteapp.model.TransactionUiModel(
            id = transaction.id,
            title = transaction.title,
            date = transaction.date, // You might want to format this date too
            amountFormatted = "Rp " + String.format(java.util.Locale.US, "%,d", transaction.amount),
            isIncome = transaction.isIncome,
            icon = getCategoryIcon(transaction.category)
        )
    }

    private fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
        return when (category) {
            "Makanan" -> androidx.compose.material.icons.Icons.Default.Restaurant
            "Transport" -> androidx.compose.material.icons.Icons.Default.DirectionsCar
            "Belanja" -> androidx.compose.material.icons.Icons.Default.ShoppingCart
            "Tagihan" -> androidx.compose.material.icons.Icons.Default.Receipt
            "Hiburan" -> androidx.compose.material.icons.Icons.Default.Movie
            "Gaji" -> androidx.compose.material.icons.Icons.Default.AttachMoney
            "Bonus" -> androidx.compose.material.icons.Icons.Default.Star
            "Penjualan" -> androidx.compose.material.icons.Icons.Default.Store
            "Investasi" -> androidx.compose.material.icons.Icons.Default.TrendingUp
            else -> androidx.compose.material.icons.Icons.Default.Category
        }
    }

    // helper to compute stats from an in-memory list
    private fun loadStatsFromList(transactions: List<Transaction>) {
        val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
        val totalExpense = transactions.filter { !it.isIncome }.sumOf { it.amount }
        _stats.value = mapOf(
            "total_income" to totalIncome,
            "total_expense" to totalExpense,
            "balance" to (totalIncome - totalExpense)
        )
    }

    // create simple dummy list (10 items) for fallback
    private fun createDummyTransactions(userId: String): List<Transaction> {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val baseDate = sdf.format(Date())

        return (1..10).map { i ->
            Transaction(
                id = "local-$i",
                userId = userId,
                title = when (i % 3) {
                    0 -> "Gaji"
                    1 -> "Makan"
                    else -> "Belanja"
                },
                amount = when (i % 3) {
                    0 -> 1_000_000L
                    1 -> 45_000L
                    else -> 250_000L
                },
                isIncome = (i % 3 == 0),
                category = when (i % 3) {
                    0 -> "Salary"
                    1 -> "Food"
                    else -> "Shopping"
                },
                date = baseDate,
                notes = null
            )
        }
    }
}
