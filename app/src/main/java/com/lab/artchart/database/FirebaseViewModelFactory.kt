package com.lab.artchart.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FirebaseViewModelFactory(private val repository: FirebaseRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST") // suppression safe because we check type in if statement
    override fun<T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FirebaseViewModel::class.java)) {
            return FirebaseViewModel(repository) as T
        }

        // this factory can only be used to create FireBaseViewModel
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}