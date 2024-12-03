package com.lab.artchart.ui.search

import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.lab.artchart.database.ReviewViewModel
import com.lab.artchart.database.UserViewModel
import com.lab.artchart.databinding.ActivityViewReviewBinding

class ViewReviewsActivity: AppCompatActivity() {
    private lateinit var binding: ActivityViewReviewBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var reviewViewModel: ReviewViewModel

    // List views
    private lateinit var artListView: ListView
    private lateinit var adapter: ReviewListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        reviewViewModel = ViewModelProvider(this)[ReviewViewModel::class.java]

        // load users
        userViewModel.loadUsersByUid()

        // observe remote database items and update listview
        artListView = binding.reviewListView
        adapter = ReviewListAdapter(this, listOf(), mapOf())
        artListView.adapter = adapter
        userViewModel.usersByUid.observe(this) {
            adapter.replaceUserMap(it)
            adapter.notifyDataSetChanged()
        }

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
            adapter.replaceReviewList(reviews)
            adapter.notifyDataSetChanged()
        }
    }

    private fun getReviewListForUser() {
        reviewViewModel.allReviewsForUser.observe(this) { reviews ->
            adapter.replaceReviewList(reviews)
            adapter.notifyDataSetChanged()
        }
    }
}