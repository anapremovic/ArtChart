package com.lab.artchart.database

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData

class FirebaseViewModel(private val repository: FirebaseRepository) : ViewModel() {
    // get flow object as live data
    val allArtworks = repository.getAllArtwork().asLiveData()

    fun saveArtwork(artwork: Artwork, imageUri: Uri) {
        repository.saveArtwork(artwork, imageUri)
    }
}