package com.lab.artchart.ui.search

import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.lab.artchart.database.ReviewViewModel
import com.lab.artchart.databinding.ActivityViewReviewBinding

class ViewReviewsActivity: AppCompatActivity() {
    private lateinit var binding: ActivityViewReviewBinding
    private lateinit var reviewViewModel: ReviewViewModel

    // List views
    private lateinit var artListView: ListView
    private lateinit var arrayAdapter: ReviewListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        reviewViewModel = ViewModelProvider(this)[ReviewViewModel::class.java]

        // set up list view
        artListView = binding.reviewListView
        arrayAdapter = ReviewListAdapter(this, listOf())
        artListView.adapter = arrayAdapter

        // extract extras
        val artId = intent.getStringExtra("artId")
        val uid = intent.getStringExtra("uid")

        // update list view depending on if entered from ArtInfo page or ProfileFragment
        if (artId != null) {
            Log.d("VIEW_REVIEWS_ACT", "Displaying reviews for art with ID $artId")
            reviewViewModel.loadAllReviewsForArtwork(artId)
            getReviewListForArt()
        } else if (uid != null) {
            Log.d("VIEW_REVIEWS_ACT", "Displaying reviews for user with ID $uid")
            reviewViewModel.loadAllReviewsForUser(uid)
            getReviewListForUser()
        }

        // button listeners
        binding.backButtonViewReview.setOnClickListener {
            finish()
        }
    }

    private fun getReviewListForArt() {
        reviewViewModel.allReviewsForArtwork.observe(this) { reviews ->
            arrayAdapter.replace(reviews)
            arrayAdapter.notifyDataSetChanged()
        }
    }

    private fun getReviewListForUser() {
        reviewViewModel.allReviewsForUser.observe(this) { reviews ->
            arrayAdapter.replace(reviews)
            arrayAdapter.notifyDataSetChanged()
        }
    }
}