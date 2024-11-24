package com.lab.artchart.database

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class UserAuthenticationViewModel : ViewModel() {
    val currentUser = currentUserFlow.asLiveData()

    // sign in
    var invalidUser = MutableLiveData<Boolean>()
    var signInSuccessful = MutableLiveData<Boolean>()

    // sign up
    var emailError = MutableLiveData<String>()
    var passwordError = MutableLiveData<String>()
    var passwordVerifyError = MutableLiveData<String>()
    var alreadyExists = MutableLiveData<Boolean>()
    var signUpSuccessful = MutableLiveData<Boolean>()

    // flow object of currently authenticated user
    private val currentUserFlow: Flow<FirebaseUser?>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                this.trySend(auth.currentUser)
            }
            Firebase.auth.addAuthStateListener(listener)

            // remove listener on app closed
            awaitClose {
                Firebase.auth.removeAuthStateListener(listener)
            }
        }

    // call Firebase API to sign user in
    fun signIn(email: String, password: String) {
        if (verifyEmailAndPasswordFormat(email, password)) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Firebase.auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                        signInSuccessful.postValue(true)
                    }
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
    fun signUp(email: String, password: String, passwordVerify: String) {
        if (verifyEmailAndPasswordFormat(email, password) && verifyPasswordSignUpRequirements(password, passwordVerify)) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Firebase.auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
                        signUpSuccessful.postValue(true)
                    }
                } catch (e: FirebaseAuthUserCollisionException) {
                    Log.d("SIGN_IN_ACT", "User with email $email already exists", e)
                    alreadyExists.postValue(true)
                } catch (e: Exception) {
                    Log.e("SIGN_IN_ACT", "Failed to create account for user with email $email", e)
                }
            }
        }
    }

    private fun verifyEmailAndPasswordFormat(email: String, password: String): Boolean {
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

    private fun verifyPasswordSignUpRequirements(password: String, passwordVerify: String): Boolean {
        if (password.length < 6) {
            passwordError.value = "Password must be at least 6 characters long"
            return false
        }
        if (password != passwordVerify) {
            passwordVerifyError.value = "Passwords do not match"
            return false
        }

        return true
    }
}