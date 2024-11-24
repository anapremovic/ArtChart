package com.lab.artchart.util

import android.text.InputType
import android.widget.CheckBox
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import com.lab.artchart.database.UserAuthenticationViewModel

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

    // observe errors for user authentication pages
    fun updateErrorMessages(userAuthenticationViewModel: UserAuthenticationViewModel,
                            observerOwner: LifecycleOwner, email: EditText, password: EditText) {
        userAuthenticationViewModel.emailError.observe(observerOwner) {
            email.error = it
        }
        userAuthenticationViewModel.passwordError.observe(observerOwner) {
            password.error = it
        }
    }
}