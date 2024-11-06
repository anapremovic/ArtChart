package com.lab.artchart.database

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData

class FirebaseViewModel(private val repository: FirebaseRepository) : ViewModel() {
    val allArtworks = repository.getAllArtwork().asLiveData()

    fun insertArtwork(artwork: Artwork, imageUri: Uri) {
        repository.saveArtwork(artwork, imageUri)
    }
}