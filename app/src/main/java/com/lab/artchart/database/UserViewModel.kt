package com.lab.artchart.database

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

    // TODO: profile picture functionality
    private var storageReference: StorageReference = FirebaseStorage.getInstance().getReference("user_profile_pictures")

    var username = MutableLiveData<String>()

    fun saveUser(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            userReference.child(user.authUid).setValue(user).await()
            Log.i("USER_VIEW_MODEL", "Saved User ${user.authUid}")
        }
    }

    fun fetchUserByUid(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            userReference.child(uid).child("username").get()
                .addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.exists()) {
                        username.postValue(dataSnapshot.getValue(String::class.java))
                    } else {
                        username.postValue("")
                        Log.w("USER_VIEW_MODEL", "Username empty for user $uid")
                    }
                }
                .addOnFailureListener { e ->
                    username.postValue("")
                    Log.e("USER_VIEW_MODEL", "Failed to fetch username for user $uid", e)
                }
        }
    }
}