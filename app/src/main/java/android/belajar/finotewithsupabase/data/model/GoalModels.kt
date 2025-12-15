package android.belajar.finotewithsupabase.data.model

import android.belajar.finotewithsupabase.utils.DateUtils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.concurrent.TimeUnit

// Enum untuk Tipe Transaksi
@Serializable
enum class TransactionType { DEPOSIT, WITHDRAW }

// Model untuk Tabel 'goals_transactions'
@Serializable
data class GoalTransaction(
    val id: String = "",
    @SerialName("goal_id") val goalId: String = "",
    val type: String = "deposit", // Supabase simpan sbg string lowercase
    val amount: Double,
    @SerialName("created_at") val createdAt: String? = null, // Format ISO String
    val note: String? = "" // Opsional, jika di DB ada kolom note
)

// Model untuk Tabel 'goals'
@Serializable
data class FinoteGoal(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val title: String = "",

    @SerialName("target_amount") val targetAmount: Double = 0.0,
    @SerialName("current_amount") val currentAmount: Double = 0.0,

    @SerialName("start_date_millis") val startDateMillis: Long? = null,
    @SerialName("end_date_millis") val endDateMillis: Long? = null,

    @SerialName("image_uri") val imageUri: String? = null,
    val description: String? = null,

    // --- PERHATIKAN BAGIAN INI (Updated ke snake_case) ---
    @SerialName("auto_deduct") val autoDeduct: Boolean = false,
    @SerialName("auto_deduct_frequency") val autoDeductFrequency: String = "Mati",
    @SerialName("auto_deduct_amount") val autoDeductAmount: Double = 0.0,
    @SerialName("auto_deduct_time") val autoDeductTime: String = "",
    @SerialName("auto_deduct_day") val autoDeductDay: String = "",
    @SerialName("auto_deduct_date") val autoDeductDate: String = ""
) {
    // --- Helper Properties (Client Side Calculation) ---

    val progress: Float
        get() = if (targetAmount > 0) {
            (currentAmount.toFloat() / targetAmount.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val remainingAmount: Double get() = targetAmount - currentAmount
    val remainingDays: Long
        get() {
            if (endDateMillis == null) return 0
            val today = System.currentTimeMillis()
            val diff = endDateMillis - today
            return if (diff < 0) 0 else TimeUnit.MILLISECONDS.toDays(diff) + 1
        }

    val dateRangeString: String
        get() {
            if (startDateMillis == null || endDateMillis == null) return "-"
            return "${DateUtils.convertMillisToDate(startDateMillis)} - ${DateUtils.convertMillisToDate(endDateMillis)}"
        }
}

@Serializable
data class TransactionParams(
    val p_user: String,
    val p_goal: String,
    val p_amount: Double
)

@Serializable
data class UserBalance(
    // Kita hanya butuh field balance, karena user_id diambil dari session
    val balance: Double = 0.0,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
