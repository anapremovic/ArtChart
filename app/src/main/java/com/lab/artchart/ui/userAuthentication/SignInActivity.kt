package com.lab.artchart.ui.userAuthentication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.lab.artchart.database.UserAuthenticationViewModel
import com.lab.artchart.databinding.ActivitySignInBinding
import com.lab.artchart.util.PasswordUtils

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
        }
        // on successful sign in
        userAuthenticationViewModel.signInSuccessful.observe(this) {
            Toast.makeText(this, "Signed in to ArtChart", Toast.LENGTH_LONG).show()
            finish()
        }
        // go back to previous page
        binding.backButton.setOnClickListener {
            finish()
        }

        // update UI
        updateErrors()
        PasswordUtils.handleShowPasswordCheckBox(listOf(binding.password), binding.showPasswordCheckbox)

        // go to sign up screen
        binding.signUpLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun updateErrors() {
        userAuthenticationViewModel.emailError.observe(this) {
            binding.email.error = it
        }
        userAuthenticationViewModel.passwordError.observe(this) {
            binding.password.error = it
        }
        userAuthenticationViewModel.invalidUser.observe(this) {
            if (it) {
                Toast.makeText(this, "Error validating credentials due to invalid username or password", Toast.LENGTH_LONG).show()
            }
        }
    }
}