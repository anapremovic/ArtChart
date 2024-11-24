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

    var signUpSuccessful = MutableLiveData<Boolean>()
    var signInSuccessful = MutableLiveData<Boolean>()

    var emailError = MutableLiveData<String>()
    var passwordError = MutableLiveData<String>()
    var passwordVerifyError = MutableLiveData<String>()
    var toastError = MutableLiveData<String>()

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
        // check formatting
        if (!verifyEmailAndPasswordFormat(email, password)) {
            return
        }

        // sign in
        CoroutineScope(Dispatchers.IO).launch {
            Firebase.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        signInSuccessful.postValue(true)
                    } else {
                        handleUnsuccessfulSignIn(task.exception, email)
                    }
                }
        }
    }

    // log and notify user when sign in fails
    private fun handleUnsuccessfulSignIn(exception: Exception?, email: String) {
        if (exception is FirebaseAuthInvalidCredentialsException) {
            Log.d("SIGN_IN_ACT", "User email and password combination invalid for user with email $email", exception)
            toastError.postValue("Error validating credentials due to invalid username or password")
        } else {
            Log.e("SIGN_IN_ACT", "Failed to sign in user with email $email", exception)
            toastError.postValue("Sign in error")
        }
    }

    // call Firebase API to create account for user
    fun signUp(email: String, password: String, passwordVerify: String) {
        // check formatting and password
        if (!verifyEmailAndPasswordFormat(email, password) || !verifyPasswordSignUpRequirements(password, passwordVerify)) {
            return
        }

        // sign up
        CoroutineScope(Dispatchers.IO).launch {
            Firebase.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        signUpSuccessful.postValue(true)
                    } else {
                        handleUnsuccessfulSignUp(task.exception, email)
                    }
                }
        }
    }

    // log and notify user when sign up fails
    private fun handleUnsuccessfulSignUp(exception: Exception?, email: String) {
        if (exception is FirebaseAuthUserCollisionException) {
            Log.d("SIGN_IN_ACT", "User with email $email already exists", exception)
            toastError.postValue("This email is already associated with an account")
        } else {
            Log.e("SIGN_IN_ACT", "Failed to create account for user with email $email", exception)
            toastError.postValue("Sign up error")
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