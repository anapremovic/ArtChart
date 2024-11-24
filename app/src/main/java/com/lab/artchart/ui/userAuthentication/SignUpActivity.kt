package com.lab.artchart.ui.userAuthentication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.lab.artchart.database.UserAuthenticationViewModel
import com.lab.artchart.databinding.ActivitySignUpBinding
import com.lab.artchart.ui.MainActivity
import com.lab.artchart.util.UserAuthenticationUtils

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
            userAuthenticationViewModel.signUp(
                binding.email.text.toString(),
                binding.password.text.toString(),
                binding.passwordVerify.text.toString())
        }
        // on successful sign up navigate to ProfileActivity
        userAuthenticationViewModel.signUpSuccessful.observe(this) {
            Toast.makeText(this, "Account created successfully", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("NAVIGATE_TO", "ProfileFragment")
            // ensure we don't create new instances of MainActivity and fragments
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
        // go back to previous page
        binding.backButton.setOnClickListener {
            finish()
        }

        // update UI
        updateErrors()
        UserAuthenticationUtils.handleShowPasswordCheckBox(listOf(binding.password, binding.passwordVerify), binding.showPasswordCheckbox)
    }

    // update UI or toast when there is an error with the sign up
    private fun updateErrors() {
        UserAuthenticationUtils.updateErrorMessages(userAuthenticationViewModel, this, binding.email, binding.password)
        userAuthenticationViewModel.passwordVerifyError.observe(this) {
            binding.passwordVerify.error = it
        }
        userAuthenticationViewModel.toastError.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }
    }
}