package ys.mobile.finoteapp.data.repository

import android.util.Log
import ys.mobile.finoteapp.data.service.TransactionService
import ys.mobile.finoteapp.model.Transaction
import ys.mobile.finoteapp.supabase.SupabaseClient

class TransactionRepository {

    // ✅ INI WAJIB ADA — supaya Retrofit ke Supabase berjalan
    private val transactionService =
        SupabaseClient.createService(TransactionService::class.java)

    /**
     * Mengambil semua transaksi untuk user tertentu
     */
    suspend fun getTransactions(userId: String): List<Transaction> {
        Log.d("SUPABASE_DEBUG", "➡️ getTransactions() called with userId = $userId")

        return try {
            val filter = "eq.$userId"
            Log.d("SUPABASE_DEBUG", "➡️ Query: user_id = $filter")

            val result = transactionService.getTransactions(filter)
            Log.d("SUPABASE_DEBUG", "✅ Response count: ${result.size}")

            result
        } catch (e: Exception) {
            Log.e("SUPABASE_DEBUG", "❌ Error getTransactions: ${e.message}")
            throw Exception("Gagal mengambil transaksi: ${e.message}", e)
        }
    }

    /**
     * Mengambil transaksi berdasarkan kategori
     */
    suspend fun getTransactionsByCategory(userId: String, category: String): List<Transaction> {
        return try {
            transactionService.getTransactionsByCategory(
                "eq.$userId",
                "eq.$category"
            )
        } catch (e: Exception) {
            throw Exception("Gagal mengambil transaksi berdasarkan kategori: ${e.message}", e)
        }
    }

    /**
     * Menambahkan transaksi baru
     */
    suspend fun addTransaction(transaction: Transaction): Transaction {
        return try {
            val result = transactionService.addTransaction(transaction)
            result.first()
        } catch (e: Exception) {
            throw Exception("Gagal menambahkan transaksi: ${e.message}", e)
        }
    }

    /**
     * Memperbarui transaksi
     */
    suspend fun updateTransaction(transaction: Transaction): Transaction {
        return try {
            val result = transactionService.updateTransaction(
                "eq.${transaction.id}",
                transaction
            )
            result.firstOrNull() ?: transaction
        } catch (e: Exception) {
            throw Exception("Gagal memperbarui transaksi: ${e.message}", e)
        }
    }

    /**
     * Menghapus transaksi
     */
    suspend fun deleteTransaction(transactionId: String) {
        try {
            transactionService.deleteTransaction("eq.$transactionId")
        } catch (e: Exception) {
            throw Exception("Gagal menghapus transaksi: ${e.message}", e)
        }
    }

    /**
     * Statistik transaksi
     */
    suspend fun getTransactionStats(userId: String): Map<String, Long> {
        return try {
            val transactions = getTransactions(userId)

            val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
            val totalExpense = transactions.filter { !it.isIncome }.sumOf { it.amount }

            mapOf(
                "total_income" to totalIncome,
                "total_expense" to totalExpense,
                "balance" to (totalIncome - totalExpense)
            )
        } catch (e: Exception) {
            throw Exception("Gagal mengambil statistik transaksi: ${e.message}", e)
        }
    }
}
