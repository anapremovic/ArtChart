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
    val artworkStatsByArtId = MutableLiveData<Map<String, ArtworkStats>>()

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

    // delete all reviews for a given user
    fun deleteReviewsForUser(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // query reviews where uid matches
                reviewsReference.orderByChild("uid").equalTo(uid).get().await().children.forEach { snapshot ->
                    // delete each matching review
                    snapshot.ref.removeValue().await()
                }
                Log.i("REVIEW_VIEW_MODEL", "Successfully deleted all reviews for user with ID $uid")
            } catch (e: Exception) {
                Log.e("REVIEW_VIEW_MODEL", "Failed to delete reviews for user with ID $uid: $e")
            }
        }
    }

    // gets average rating for and number of reviews for each artwork and puts them in a map
    fun loadArtworkStatsByArtId() {
        viewModelScope.launch {
            reviewsReference.get().await().let { snapshot ->
                // group artworks by art ID
                val reviewsByArtId = snapshot.children
                    .mapNotNull { it.getValue(Review::class.java) }
                    .groupBy { it.artId!! }

                // if there are reviews, extract average rating and number of reviews
                val stats = reviewsByArtId.mapValues { (_, reviews) ->
                    var averageRating = 0f
                    var reviewCount = 0
                    if (reviews.isNotEmpty()) {
                        averageRating = reviews.mapNotNull { it.rating }.average().toFloat()
                        reviewCount = reviews.size
                    }
                    ArtworkStats(averageRating, reviewCount)
                }

                artworkStatsByArtId.postValue(stats)
            }
        }
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