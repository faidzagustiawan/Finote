package android.belajar.finotewithsupabase.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun convertMillisToDate(millis: Long?): String {
        if (millis == null) return "-"
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        return formatter.format(Date(millis))
    }

    fun formatTransactionDate(isoDateString: String?): String {
        if (isoDateString == null) return "-"
        // Supabase mengembalikan format ISO 8601, kita parse simpel saja
        // Jika format string ribet, bisa pakai library java.time (Instant)
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            val date = parser.parse(isoDateString.take(10)) // Ambil 10 karakter pertama (YYYY-MM-DD)
            formatter.format(date ?: Date())
        } catch (e: Exception) {
            isoDateString
        }
    }
}