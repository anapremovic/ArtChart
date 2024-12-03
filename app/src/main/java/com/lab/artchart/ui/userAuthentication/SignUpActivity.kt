package com.lab.artchart.ui.userAuthentication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.lab.artchart.database.ReviewViewModel
import com.lab.artchart.database.UserAuthenticationViewModel
import com.lab.artchart.database.UserAuthenticationViewModelFactory
import com.lab.artchart.database.UserViewModel
import com.lab.artchart.databinding.ActivitySignUpBinding
import com.lab.artchart.util.UserAuthenticationUtils

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var reviewViewModel: ReviewViewModel
    private lateinit var userAuthenticationViewModel: UserAuthenticationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        reviewViewModel = ViewModelProvider(this)[ReviewViewModel::class.java]
        val userAuthenticationViewModelFactory = UserAuthenticationViewModelFactory(userViewModel, reviewViewModel)
        userAuthenticationViewModel = ViewModelProvider(this, userAuthenticationViewModelFactory)[UserAuthenticationViewModel::class.java]

        // create account with user input if fields correct
        binding.signUpButton.setOnClickListener {
            val username = binding.username.text.toString()
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val passwordVerify = binding.passwordVerify.text.toString()

            if (UserAuthenticationUtils.verifyUsernameRequirements(username, binding.username) &&
                UserAuthenticationUtils.verifyEmailFormat(email, binding.email) &&
                UserAuthenticationUtils.verifyPasswordNotBlank(password, binding.password) &&
                UserAuthenticationUtils.verifyPasswordRequirements(password, passwordVerify, binding.password, binding.passwordVerify)) {
                userAuthenticationViewModel.signUp(email, password, username)
            }
        }
        // on successful sign up navigate to ProfileActivity
        userAuthenticationViewModel.signUpSuccessful.observe(this) {
            Toast.makeText(this, "Account created successfully, please verify your email to sign in", Toast.LENGTH_LONG).show()
            UserAuthenticationUtils.navigateToSignInScreenFromActivity(this)
            finish()
        }
        // go back to previous page
        binding.backButton.setOnClickListener {
            finish()
        }

        // update UI
        userAuthenticationViewModel.toastError.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }
        UserAuthenticationUtils.handleShowPasswordCheckBox(listOf(binding.password, binding.passwordVerify), binding.showPasswordCheckbox)
    }
}