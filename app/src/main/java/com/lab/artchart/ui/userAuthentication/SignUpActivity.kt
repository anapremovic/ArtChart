package com.lab.artchart.ui.userAuthentication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.lab.artchart.database.UserAuthenticationViewModel
import com.lab.artchart.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var userAuthenticationViewModel: UserAuthenticationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userAuthenticationViewModel = ViewModelProvider(this)[UserAuthenticationViewModel::class.java]

        // create account with user input
        binding.signUpButton.setOnClickListener {
            userAuthenticationViewModel.verifyInfoAndSignUp(
                binding.email.text.toString(),
                binding.password.text.toString(),
                binding.passwordVerify.text.toString())
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
        userAuthenticationViewModel.passwordVerifyError.observe(this) {
            binding.passwordVerify.error = it
        }
    }
}