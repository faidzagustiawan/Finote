package ys.mobile.finoteapp.data.repository

import android.util.Log
import io.github.jan.supabase.postgrest.from
import ys.mobile.finoteapp.data.remote.SupabaseClient.client
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
        return try {
            client.from("transactions")
                .select {
                    filter { eq("user_id", userId) }
                }.decodeList<Transaction>()
        } catch (e: Exception) {
            Log.e("SUPABASE_DEBUG", "Error getTransactions: ${e.message}")
            throw e
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
    suspend fun addTransaction(transaction: Transaction) {
        try {
            // Langsung insert menggunakan model Transaction
            client.from("transactions").insert(transaction)
        } catch (e: Exception) {
            Log.e("SUPABASE_DEBUG", "Error addTransaction: ${e.message}")
            throw e
        }
    }

    suspend fun updateTransaction(transaction: Transaction) {
        try {
            client.from("transactions").update(transaction) {
                filter { eq("id", transaction.id) }
            }
        } catch (e: Exception) {
            Log.e("SUPABASE_DEBUG", "Error updateTransaction: ${e.message}")
            throw e
        }
    }

    /**
     * Menghapus transaksi
     */
    suspend fun deleteTransaction(transactionId: String) {
        try {
            client.from("transactions").delete {
                filter { eq("id", transactionId) }
            }
        } catch (e: Exception) {
            Log.e("SUPABASE_DEBUG", "Error deleteTransaction: ${e.message}")
            throw e
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
