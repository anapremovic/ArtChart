package com.lab.artchart.database

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UserViewModel : ViewModel() {
    private var databaseRoot: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var userReference: DatabaseReference = databaseRoot.reference.child("users")
    private var storageReference: StorageReference = FirebaseStorage.getInstance().getReference("user_profile_pictures")


}