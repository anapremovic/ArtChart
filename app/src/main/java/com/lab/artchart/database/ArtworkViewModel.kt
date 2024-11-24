package com.lab.artchart.database

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ArtworkViewModel : ViewModel() {
    private var databaseRoot: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var artworkReference: DatabaseReference = databaseRoot.reference.child("artwork")
    private var storageReference: StorageReference = FirebaseStorage.getInstance().getReference("artwork_images")

    // get flow object as live data
    val allArtworks = getAllArtwork().asLiveData()

    fun saveArtwork(artwork: Artwork, imageUri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            val artworkId = artworkReference.push().key!!

            // save image to storage and get URL
            val uploadTask = storageReference.child(artworkId).putFile(imageUri).await()
            val imageUrl = uploadTask.storage.downloadUrl.await()

            // update URL and save artwork to database
            artwork.imageUrl = imageUrl.toString()
            artworkReference.child(artworkId).setValue(artwork).await()

            Log.i("FIREBASE_REPO", "Saved Artwork $artworkId")
        }
    }

    private fun getAllArtwork(): Flow<List<Artwork>> {
        // return flow object using a callback
        return callbackFlow {
            val listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // convert DataSnapshot to list of artworks
                    val artworkList = dataSnapshot.children.mapNotNull {
                        it.getValue(Artwork::class.java)
                    }
                    // send list to flow object
                    trySend(artworkList).isSuccess
                }

                override fun onCancelled(error: DatabaseError) {
                    cancel("Error reading all artwork from database", error.toException())
                }
            }

            // attach listener to database reference
            artworkReference.addValueEventListener(listener)

            // remove the listener when the flow is closed
            awaitClose { artworkReference.removeEventListener(listener) }
        }
    }
}