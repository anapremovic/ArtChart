package com.lab.artchart.ui.userAuthentication

import android.text.InputType
import android.widget.CheckBox
import android.widget.EditText

object PasswordUtils {
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
}