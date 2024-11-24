package com.lab.artchart.util

import android.text.InputType
import android.util.Patterns
import android.widget.CheckBox
import android.widget.EditText

// helper functions for user authentication
object UserAuthenticationUtils {
    // show and hide text depending on check box status
    fun handleShowPasswordCheckBox(passwordFields: List<EditText>, showPasswordCheckBox: CheckBox) {
        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            for (field in passwordFields) {
                if (isChecked) {
                    field.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                } else {
                    field.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
            }
        }
    }

    fun verifyUsernameRequirements(username: String, usernameInput: EditText): Boolean {
        if (username.isBlank()) {
            usernameInput.error = "Username is required"
            return false
        }

        if (username.length in 4..15) {
            usernameInput.error = "Username must be between 4 and 15 characters long"
            return false
        }

        return true
    }

    fun verifyEmailFormat(email: String, emailInput: EditText): Boolean {
        if (email.isBlank()) {
            emailInput.error = "Email is required"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Please enter a valid email"
            return false
        }

        return true
    }

    fun verifyPasswordNotBlank(password: String, passwordInput: EditText): Boolean {
        if (password.isBlank()) {
            passwordInput.error = "Password is required"
            return false
        }

        return true
    }

    fun verifyPasswordRequirements(password: String, passwordVerify: String, passwordInput: EditText, passwordVerifyInput: EditText): Boolean {
        if (password.length < 6) {
            passwordInput.error = "Password must be at least 6 characters long"
            return false
        }
        if (password != passwordVerify) {
            passwordVerifyInput.error = "Passwords do not match"
            return false
        }

        return true
    }
}