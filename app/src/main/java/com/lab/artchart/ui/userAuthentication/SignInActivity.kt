package com.lab.artchart.ui.userAuthentication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.lab.artchart.database.UserAuthenticationViewModel
import com.lab.artchart.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var userAuthenticationViewModel: UserAuthenticationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userAuthenticationViewModel = ViewModelProvider(this)[UserAuthenticationViewModel::class.java]

        // sign into existing account with user input or show error
        binding.signInButton.setOnClickListener {
            userAuthenticationViewModel.signIn(binding.email.text.toString(), binding.password.text.toString())
            if (userAuthenticationViewModel.invalidUser.value == true) {
                Toast.makeText(this, "Error validating credentials due to invalid username or password", Toast.LENGTH_LONG).show()
            }
        }

        // update UI
        updateErrors()
    }

    private fun updateErrors() {
        userAuthenticationViewModel.emailError.observe(this) {
            binding.email.error = it
        }
        userAuthenticationViewModel.passwordError.observe(this) {
            binding.password.error = it
        }
    }
}