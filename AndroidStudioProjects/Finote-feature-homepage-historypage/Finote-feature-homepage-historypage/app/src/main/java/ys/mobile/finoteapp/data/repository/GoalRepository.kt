package ys.mobile.finoteapp.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import ys.mobile.finoteapp.data.model.FinoteGoal
import ys.mobile.finoteapp.data.model.GoalTransaction
import ys.mobile.finoteapp.data.model.TransactionParams
import ys.mobile.finoteapp.data.model.UserBalance
import ys.mobile.finoteapp.data.remote.SupabaseClient
import androidx.compose.runtime.mutableStateListOf
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

object GoalRepository {
    private val client = SupabaseClient.client
    private val scope = CoroutineScope(Dispatchers.IO)

    val goals = mutableStateListOf<FinoteGoal>()
    val currentTransactions = mutableStateListOf<GoalTransaction>()
    var mainBalance = androidx.compose.runtime.mutableDoubleStateOf(0.0)

    fun fetchMainBalance() {
        scope.launch {
            try {
                // Mengambil 1 baris data dari tabel users_balance milik user yang login
                val result = client.from("users_balance")
                    .select()
                    .decodeSingleOrNull<UserBalance>() // Gunakan SingleOrNull jaga-jaga data belum ada

                withContext(Dispatchers.Main) {
                    mainBalance.doubleValue = result?.balance ?: 0.0
                }
            } catch (e: Exception) {
                Log.e("SUPABASE", "Error fetch balance", e)
            }
        }
    }
    fun fetchGoals() {
        scope.launch {
            try {
                val result = client.from("goals").select().decodeList<FinoteGoal>()
                withContext(Dispatchers.Main) {
                    goals.clear()
                    goals.addAll(result)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE", "Error fetching goals", e)
            }
        }
    }

    // --- FUNGSI UPDATE ---
    fun updateGoal(updatedGoal: FinoteGoal) {
        scope.launch {
            try {
                // (Opsional) Safety check
                if (updatedGoal.userId.isEmpty()) {
                    Log.e("SUPABASE", "Update gagal: User ID hilang")
                    // Anda bisa mengambil user ID dari session sebagai fallback:
                    // val currentUser = client.auth.currentUserOrNull()
                    // val fixedGoal = updatedGoal.copy(userId = currentUser?.id ?: "")
                    // Lalu gunakan fixedGoal untuk update
                    return@launch
                }

                client.from("goals").update(updatedGoal) {
                    filter { eq("id", updatedGoal.id) }
                }
                fetchGoals()
            } catch (e: Exception) {
                Log.e("SUPABASE", "Error updating goal", e)
            }
        }
    }

    // --- FUNGSI DELETE ---
    fun deleteGoal(goalId: String) {
        scope.launch {
            try {
                client.from("goals").delete {
                    filter { eq("id", goalId) }
                }
                withContext(Dispatchers.Main) {
                    goals.removeAll { it.id == goalId }
                }
            } catch (e: Exception) {
                Log.e("SUPABASE", "Error deleting goal", e)
            }
        }
    }

    // --- FUNGSI ADD dengan UPLOAD IMAGE ---
    fun addGoal(goal: FinoteGoal, imageUri: Uri? = null, context: Context? = null) {
        scope.launch {
            try {
                // FALLBACK: Use Demo ID if not logged in
                val user = client.auth.currentUserOrNull() ?: return@launch
                var finalImageUrl = goal.imageUri

                // 1. Upload Image jika ada
                if (imageUri != null && context != null) {
                    val fileName = "${user.id}/${UUID.randomUUID()}.jpg"
                    val byteArray = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }

                    if (byteArray != null) {
                        val bucket = client.storage.from("goal-images") // Pastikan bucket ini ada di Supabase
                        bucket.upload(fileName, byteArray)
                        finalImageUrl = bucket.publicUrl(fileName)
                    }
                }

                // 2. Simpan Data ke DB
                val newId = if (goal.id.isEmpty()) UUID.randomUUID().toString() else goal.id
                val goalToInsert = goal.copy(
                    id = newId,
                    userId = user.id, // Use fallback ID
                    imageUri = finalImageUrl
                )

                client.from("goals").insert(goalToInsert)
                fetchGoals()
            } catch (e: Exception) {
                Log.e("SUPABASE", "Error adding goal", e)
            }
        }
    }

    fun fetchTransactions(goalId: String) {
        scope.launch {
            try {
                val result = client.from("goals_transactions")
                    .select {
                        filter { eq("goal_id", goalId) }
                        order("created_at", Order.DESCENDING)
                    }.decodeList<GoalTransaction>()

                withContext(Dispatchers.Main) {
                    currentTransactions.clear()
                    currentTransactions.addAll(result)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE", "Error fetching transactions", e)
            }
        }
    }

    fun deposit(goalId: String, amount: Double) {
        scope.launch {
            try {
                val user = client.auth.currentUserOrNull() ?: return@launch
                val params = TransactionParams(user.id, goalId, amount)
                client.postgrest.rpc("deposit_to_goal", params)
                fetchGoals()
                fetchTransactions(goalId)
            } catch (e: Exception) {
                Log.e("SUPABASE", "Deposit Error", e)
            }
        }
    }

    fun withdraw(goalId: String, amount: Double) {
        scope.launch {
            try {
                val user = client.auth.currentUserOrNull() ?: return@launch
                val params = TransactionParams(user.id, goalId, amount)
                client.postgrest.rpc("withdraw_from_goal", params)
                fetchGoals()
                fetchTransactions(goalId)
            } catch (e: Exception) {
                Log.e("SUPABASE", "Withdraw Error", e)
            }
        }
    }

    // Helper lokal
    fun getGoalById(id: String): FinoteGoal? {
        return goals.find { it.id == id }
    }
}
