package com.lab.artchart.database

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel : ViewModel() {
    private var databaseRoot: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var userReference: DatabaseReference = databaseRoot.reference.child("users")
    private var storageReference: StorageReference = FirebaseStorage.getInstance().getReference("user_profile_pictures")

    var user = MutableLiveData<User?>()
    var usernameChanged = MutableLiveData<Boolean>()
    var toastError = MutableLiveData<String>()

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

    fun fetchUserByUid(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dataSnapshot = userReference.child(uid).get().await()
                if (dataSnapshot.exists()) {
                    user.postValue(dataSnapshot.getValue(User::class.java))
                } else {
                    user.postValue(null)
                    Log.w("USER_VIEW_MODEL", "No user with ID $uid")
                    toastError.postValue("Error generating profile")
                }
            } catch (e: Exception) {
                Log.e("USER_VIEW_MODEL", "Failed to fetch user for ID $uid", e)
                user.postValue(null)
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
}