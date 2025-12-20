package ys.mobile.finoteapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class TransactionViewModel : ViewModel() {
    var nominal = mutableStateOf("")
        private set
    var category = mutableStateOf("")
        private set
    var note = mutableStateOf("")
        private set
    var date = mutableStateOf("")
        private set

    fun updateNominal(value: String) {
        nominal.value = value
    }

    fun updateCategory(value: String) {
        category.value = value
    }

    fun updateNote(value: String) {
        note.value = value
    }

    fun updateDate(value: String) {
        date.value = value
    }
}
