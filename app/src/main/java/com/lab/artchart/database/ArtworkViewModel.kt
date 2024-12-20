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

            Log.i("ARTWORK_VIEW_MODEL", "Saved Artwork $artworkId")
        }
    }

    fun toggleDetectArtField(artworkId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Locate the artwork by its ID and update the specific field
                artworkReference.child(artworkId).child("detectArt").setValue(false).await()
                Log.i("ARTWORK_VIEW_MODEL", "Updated detectArt of Artwork $artworkId to false")
            } catch (e: Exception) {
                Log.e("ARTWORK_VIEW_MODEL", "Error updating detectArt of Artwork $artworkId", e)
            }
        }
    }

    private fun getAllArtwork(): Flow<List<Artwork>> {
        // return flow object using a callback
        return callbackFlow {
            val listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // convert DataSnapshot to list of artworks and add art IDs
                    val artworkList = mutableListOf<Artwork>()
                    for (artSnapshot in dataSnapshot.children) {
                        val artwork = artSnapshot.getValue(Artwork::class.java)
                        artwork?.artId = artSnapshot.key // add art ID
                        artwork?.let { artworkList.add(it) }
                    }

                    // filter out artworks flagged as not art by AI trigger
                    val actualArtList = artworkList.filter { it.detectArt == true }

                    // send list to flow object
                    trySend(actualArtList).isSuccess
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