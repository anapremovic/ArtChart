package com.lab.artchart.database

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
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

    var signInSuccessful = MutableLiveData<Boolean>()
    var signUpSuccessful = MutableLiveData<Boolean>()
    var changeEmailSent = MutableLiveData<Boolean>()
    var passwordChanged = MutableLiveData<Boolean>()
    var deleteSuccessful = MutableLiveData<Boolean>()
    var needReAuthenticateToChangeEmail = MutableLiveData<Boolean>()
    var needReAuthenticateToChangePassword = MutableLiveData<Boolean>()
    var needReAuthenticateToDeleteAccount = MutableLiveData<Boolean>()
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
        // sign in
        CoroutineScope(Dispatchers.IO).launch {
            Firebase.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        signInSuccessful.postValue(true)
                    } else {
                        handleFirebaseError(task.exception, "Sign in error",
                            "Failed to sign in user with email $email",
                            email, "")
                    }
                }
        }
    }

    // call Firebase API to create account for user
    fun signUp(email: String, password: String) {
        // sign up
        CoroutineScope(Dispatchers.IO).launch {
            Firebase.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result.user
                        sendEmailVerification(user, email) // verify email
                    } else {
                        handleFirebaseError(task.exception, "Sign up error",
                            "Failed to create account for user with email $email",
                            email, "")
                    }
                }
        }
    }

    // call Firebase API to send verification email to newly created user
    private fun sendEmailVerification(user: FirebaseUser?, email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            user?.sendEmailVerification()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        signOut() // sign out
                        signUpSuccessful.postValue(true)
                    } else {
                        handleFirebaseError(task.exception, "Error sending verification email",
                            "Failed to send verification email to $email",
                            email, "")
                    }
                }
        }
    }

    // call Firebase API to change user email
    fun changeEmail(email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            currentUser.value?.verifyBeforeUpdateEmail(email)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        changeEmailSent.postValue(true)
                    } else {
                        handleFirebaseError(task.exception, "Error sending email-change email",
                            "Failed to send email-change email to user $email",
                            email, "changeEmail")
                    }
                }
        }
    }

    // call Firebase API to change user password
    fun changePassword(password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            currentUser.value?.updatePassword(password)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        passwordChanged.postValue(true)
                    } else {
                        handleFirebaseError(task.exception, "Password change error",
                            "Failed to change password for user with email ${currentUser.value?.email}",
                            null, "changePassword")
                    }
                }
        }
    }

    // call Firebase API to sign user out
    fun signOut() {
        CoroutineScope(Dispatchers.IO).launch {
            Firebase.auth.signOut()
        }
    }

    // call Firebase API to delete user's account
    fun deleteAccount() {
        CoroutineScope(Dispatchers.IO).launch {
            currentUser.value?.delete()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        deleteSuccessful.postValue(true)
                    } else {
                        handleFirebaseError(task.exception, "Account deletion error",
                            "Failed to delete account for user with email ${currentUser.value?.email}",
                            null, "deleteAccount")
                    }
                }
        }
    }

    // call Firebase API to re-authenticate user credentials
    fun reAuthenticate(email: String, password: String, action: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val credential = EmailAuthProvider.getCredential(email, password)
            currentUser.value?.reauthenticate(credential)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        action()
                    } else {
                        handleFirebaseError(task.exception, "Authentication error",
                            "Failed to re-authenticate user with email $email",
                            email, "")
                    }
                }
        }
    }

    // log and notify user when Firebase API call fails
    private fun handleFirebaseError(exception: Exception?, genericToast: String, genericErrorLog: String, email: String? = null, reAuthenticateReason: String) {
        when (exception) {
            is FirebaseAuthRecentLoginRequiredException -> {
                handleReAuthenticate(reAuthenticateReason)
            }
            is FirebaseAuthUserCollisionException -> {
                Log.d("USER_AUTH", "User with email $email already exists", exception)
                toastError.postValue("This email is already associated with an account")
            }
            is FirebaseAuthInvalidCredentialsException -> {
                Log.d("USER_AUTH", "User email and password combination invalid for user with email $email", exception)
                toastError.postValue("Error validating credentials due to invalid username or password")
            }
            else -> {
                Log.e("USER_AUTH", genericErrorLog, exception)
                toastError.postValue(genericToast)
            }
        }
    }

    private fun handleReAuthenticate(reason: String) {
        when (reason) {
            "changeEmail" -> {
                Log.d("USER_AUTH", "Prompting user to re-authenticate in order to change their email")
                needReAuthenticateToChangeEmail.postValue(true)
            }
            "changePassword" -> {
                Log.d("USER_AUTH", "Prompting user to re-authenticate in order to change their password")
                needReAuthenticateToChangePassword.postValue(true)
            }
            "deleteAccount" -> {
                Log.d("USER_AUTH", "Prompting user to re-authenticate in order to delete their account")
                needReAuthenticateToDeleteAccount.postValue(true)
            }
        }
    }
}