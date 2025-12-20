package ys.mobile.finoteapp.utils

import android.util.Log
import com.google.gson.Gson

object GsonUtils {

    private val gson = Gson()

    /**
     * Menghasilkan dokumen JSON dari object Kotlin.
     */
    fun generateTransactionJson(): String {

        val sample = mapOf(
            "title" to "Belanja",
            "amount" to 50000,
            "category" to "Makan",
            "is_income" to false
        )

        val json = gson.toJson(sample)
        Log.d("GSON_GENERATE", "Dokumen JSON dihasilkan: $json")

        return json
    }
}
