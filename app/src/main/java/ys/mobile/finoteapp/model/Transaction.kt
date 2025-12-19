package ys.mobile.finoteapp.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Transaction(

    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("amount")
    val amount: Long,

    @SerializedName("is_income")
    val isIncome: Boolean,

    @SerializedName("category")
    val category: String,

    @SerializedName("date")
    val date: String, // biarkan string, jangan parse timestamp

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("image_uri")
    val imageUri: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)
