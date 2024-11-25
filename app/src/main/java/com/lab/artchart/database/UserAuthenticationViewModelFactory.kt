package com.lab.artchart.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class UserAuthenticationViewModelFactory(private val userViewModel: UserViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST") // suppression safe because we check type in if statement
    override fun<T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserAuthenticationViewModel::class.java)) {
            return UserAuthenticationViewModel(userViewModel) as T
        }

        // this factory can only be used to create UserAuthenticationViewModel
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}