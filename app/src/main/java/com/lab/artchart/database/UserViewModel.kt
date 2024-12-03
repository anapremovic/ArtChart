package com.lab.artchart.database

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel : ViewModel() {
    private var databaseRoot: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var userReference: DatabaseReference = databaseRoot.reference.child("users")
    private var storageReference: StorageReference = FirebaseStorage.getInstance().getReference("user_profile_pictures")

    var currentlyAuthenticatedUser = MutableLiveData<User?>()
    var usernameChanged = MutableLiveData<Boolean>()
    var toastError = MutableLiveData<String>()
    var usersByUid = MutableLiveData<Map<String, User>>()

    fun saveUser(uid: String, user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                userReference.child(uid).setValue(user).await()
                Log.i("USER_VIEW_MODEL", "Saved user $uid")
            } catch (e: Exception) {
                Log.e("USER_VIEW_MODEL", "Failed to save user with ID $uid", e)
                toastError.postValue("Error saving user")
            }
        }
    }

    fun saveProfilePicture(uid: String, imageUri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uploadTask = storageReference.child(uid).putFile(imageUri).await()
                val imageUrl = uploadTask.storage.downloadUrl.await()
                userReference.child(uid).child("profilePictureUrl").setValue(imageUrl.toString())
                Log.i("USER_VIEW_MODEL", "Saved profile picture for user $uid")
            } catch (e: Exception) {
                Log.e("USER_VIEW_MODEL", "Failed to save profile picture for user with ID $uid", e)
                toastError.postValue("Error saving profile picture")
            }
        }
    }

    // get uid to user map
    fun loadUsersByUid() {
        viewModelScope.launch {
            userReference.get().await().let { snapshot ->
                val userMap = snapshot.children
                    .mapNotNull { childSnapshot ->
                        val user = childSnapshot.getValue(User::class.java)
                        val key = childSnapshot.key
                        if (user != null && key != null) key to user else null
                    }
                    .toMap()

                usersByUid.postValue(userMap)
            }
        }
    }


    fun fetchUserByUid(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dataSnapshot = userReference.child(uid).get().await()
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.uid = dataSnapshot.key
                    user?.let { currentlyAuthenticatedUser.postValue(it) }
                } else {
                    currentlyAuthenticatedUser.postValue(null)
                    Log.w("USER_VIEW_MODEL", "No user with ID $uid")
                    toastError.postValue("Error generating profile")
                }
            } catch (e: Exception) {
                Log.e("USER_VIEW_MODEL", "Failed to fetch user for ID $uid", e)
                currentlyAuthenticatedUser.postValue(null)
                toastError.postValue("Error generating profile")
            }
        }
    }

    fun changeUsername(uid: String, newUsername: String) {
        CoroutineScope(Dispatchers.IO).launch {
            userReference.child(uid).child("username").setValue(newUsername)
                .addOnSuccessListener {
                    Log.i("USER_VIEW_MODEL", "Changed username for user $uid to $newUsername")
                    usernameChanged.postValue(true)
                }
                .addOnFailureListener { e ->
                    Log.e("USER_VIEW_MODEL", "Failed to change username for user $uid", e)
                    toastError.postValue("Username change error")
                }
        }
    }

    fun deleteUser(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userReference.child(uid).removeValue().await()
                Log.i("USER_VIEW_MODEL", "Deleted user $uid from real-time database")

                storageReference.child(uid).delete().await()
                Log.i("USER_VIEW_MODEL", "Deleted profile picture for user $uid")

                // success message handled in UserAuthenticationViewModel
            } catch (e: StorageException) {
                Log.d("USER_VIEW_MODEL", "No profile picture, skip")
            }
            catch (e: Exception) {
                Log.e("USER_VIEW_MODEL", "Failed to delete user with ID $uid", e)
                // failure message handled in UserAuthenticationViewModel
            }
        }
    }
}