package com.lab.artchart.database

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserAuthenticationViewModel : ViewModel() {
    // sign in
    var invalidUser = MutableLiveData<Boolean>()

    // sign up
    var emailError = MutableLiveData<String>()
    var passwordError = MutableLiveData<String>()
    var passwordVerifyError = MutableLiveData<String>()
    var alreadyExists = MutableLiveData<Boolean>()

    // call Firebase API to sign user in
    fun signIn(email: String, password: String) {
        if (verifyEmailAndPassword(email, password)) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Firebase.auth.signInWithEmailAndPassword(email, password).await()
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    Log.d("SIGN_IN_ACT", "User email and password combination invalid for user with email $email", e)
                    invalidUser.postValue(true)
                } catch (e: Exception) {
                    Log.e("SIGN_IN_ACT", "Failed to sign in user with email $email", e)
                }
            }
        }
    }

    // call Firebase API to create account for user
    fun verifyInfoAndSignUp(email: String, password: String, passwordVerify: String) {
        if (verifyEmailAndPassword(email, password) && verifyPasswordsMatch(password, passwordVerify)) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Firebase.auth.createUserWithEmailAndPassword(email, password).await()
                } catch (e: FirebaseAuthUserCollisionException) {
                    Log.d("SIGN_IN_ACT", "User with email $email already exists", e)
                    alreadyExists.postValue(true)
                } catch (e: Exception) {
                    Log.e("SIGN_IN_ACT", "Failed to create account for user with email $email", e)
                }
            }
        }
    }

    private fun verifyEmailAndPassword(email: String, password: String): Boolean {
        if (email.isBlank()) {
            emailError.value = "Email is required"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError.value = "Please enter a valid email"
            return false
        }

        if (password.isBlank()) {
            passwordError.value = "Password is required"
            return false
        }

        return true
    }

    private fun verifyPasswordsMatch(password: String, passwordVerify: String): Boolean {
        if (password != passwordVerify) {
            passwordVerifyError.value = "Passwords do not match"
            return false
        }

        return true
    }
}