package ys.mobile.finoteapp.data.service

import retrofit2.http.*
import ys.mobile.finoteapp.model.Transaction

/**
 * Retrofit service untuk Transaction API di Supabase
 */
interface TransactionService {

    /**
     * Get all transactions
     * GET /transactions?user_id=eq.{userId}
     */
    @GET("transactions")
    suspend fun getTransactions(
        @Query("user_id") userIdFilter: String,
        @Query("order") order: String = "date.desc"
    ): List<Transaction>

    /**
     * Get transactions by category
     * GET /transactions?user_id=eq.{userId}&category=eq.{category}
     */
    @GET("transactions")
    suspend fun getTransactionsByCategory(
        @Query("user_id") userIdFilter: String,
        @Query("category") categoryFilter: String,
        @Query("order") order: String = "date.desc"
    ): List<Transaction>

    /**
     * Add new transaction
     * POST /transactions
     */
    @Headers("Prefer: return=representation")
    @POST("transactions")
    suspend fun addTransaction(@Body transaction: Transaction): List<Transaction>

    /**
     * Update transaction
     * PATCH /transactions?id=eq.{id}
     */
    @PATCH("transactions")
    suspend fun updateTransaction(
        @Query("id") idFilter: String,
        @Body transaction: Transaction
    ): List<Transaction>

    @DELETE("transactions")
    suspend fun deleteTransaction(
        @Query("id") idFilter: String
    )
}

