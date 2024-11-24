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

    fun verifyEmailAndPasswordFormat(email: String, password: String, emailText: EditText, passwordText: EditText): Boolean {
        if (email.isBlank()) {
            emailText.error = "Email is required"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.error = "Please enter a valid email"
            return false
        }

        if (password.isBlank()) {
            passwordText.error = "Password is required"
            return false
        }

        return true
    }
}