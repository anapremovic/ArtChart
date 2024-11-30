package com.lab.artchart.database

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ReviewViewModel : ViewModel() {
    private var databaseRoot: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var reviewsReference: DatabaseReference = databaseRoot.reference.child("reviews")

    // live data updates when load functions called
    val allReviewsForArtwork = MutableLiveData<List<Review>>()
    val allReviewsForUser = MutableLiveData<List<Review>>()

    // saves given review to Firebase
    fun saveReview(review: Review) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reviewId = reviewsReference.push().key!!
                reviewsReference.child(reviewId).setValue(review).await()
                Log.i("REVIEW_VIEW_MODEL", "Saved review $reviewId")
            } catch (e: Exception) {
                Log.e("REVIEW_VIEW_MODEL", "Failed to save review for user with ID ${review.uid}")
            }
        }
    }

    // get average rating for current artwork or null if no reviews
    fun getAverageRatingForArtworkOrNull(): Double? {
        if (allReviewsForArtwork.value.isNullOrEmpty()) {
            return null
        }
        return allReviewsForArtwork.value?.map { it.rating?.toDouble() ?: 0.0 }?.average()
    }

    // updates live data with all reviews for given artwork
    fun loadAllReviewsForArtwork(artworkId: String) {
        viewModelScope.launch {
            getAllReviewsForIdentifier(artworkId, false).collect { reviews ->
                allReviewsForArtwork.value = reviews
            }
        }
    }

    // updates live data with all reviews for given user
    fun loadAllReviewsForUser(userId: String) {
        viewModelScope.launch {
            getAllReviewsForIdentifier(userId, true).collect { reviews ->
                allReviewsForUser.value = reviews
            }
        }
    }

    // loads reviews from Firebase into flow object
    private fun getAllReviewsForIdentifier(id: String, fetchingForUser: Boolean): Flow<List<Review>> {
        // return flow object using a callback
        return callbackFlow {
            val listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // convert DataSnapshot to list of reviews
                    val reviewList = dataSnapshot.children.mapNotNull {
                        it.getValue(Review::class.java)
                    }

                    // filter out reviews not corresponding to the given ID
                    val reviewsForId = when(fetchingForUser) {
                        true -> reviewList.filter { it.uid == id } // fetch reviews for given user
                        else -> reviewList.filter { it.artId == id } // fetch reviews for given artwork
                    }

                    // send list to flow object
                    trySend(reviewsForId).isSuccess
                }

                override fun onCancelled(error: DatabaseError) {
                    cancel("Error reading reviews from database", error.toException())
                }
            }

            // attach listener to database reference
            reviewsReference.addValueEventListener(listener)

            // remove the listener when the flow is closed
            awaitClose { reviewsReference.removeEventListener(listener) }
        }
    }
}