package ys.mobile.finoteapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable // WAJIB: Agar Supabase SDK bisa membaca class ini
data class Transaction(
    @SerialName("id")
    val id: String = UUID.randomUUID().toString(),

    @SerialName("user_id") // Memetakan properti userId ke kolom user_id di DB
    val userId: String,

    @SerialName("title")
    val title: String,

    @SerialName("amount")
    val amount: Long,

    @SerialName("is_income") // Memetakan isIncome ke kolom is_income di DB
    val isIncome: Boolean,

    @SerialName("category")
    val category: String,

    @SerialName("date")
    val date: String,

    @SerialName("notes")
    val notes: String? = null,

    @SerialName("image_uri")
    val imageUri: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)