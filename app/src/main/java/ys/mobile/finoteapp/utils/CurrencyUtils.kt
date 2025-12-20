package ys.mobile.finoteapp.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    fun toRupiah(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return "Rp ${formatter.format(amount)}"
    }

    fun toRupiah(amount: Long): String {
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return "Rp ${formatter.format(amount)}"
    }
}
