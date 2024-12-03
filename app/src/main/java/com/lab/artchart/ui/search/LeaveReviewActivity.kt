package com.lab.artchart.ui.search

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.lab.artchart.R
import com.lab.artchart.database.Review
import com.lab.artchart.database.ReviewViewModel
import com.lab.artchart.database.UserViewModel
import com.lab.artchart.databinding.ActivityLeaveReviewBinding

class LeaveReviewActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLeaveReviewBinding
    private lateinit var reviewViewModel: ReviewViewModel
    private lateinit var userViewModel: UserViewModel

    // art and review info
    private var title: String? = null
    private var imageUrl: String? = null
    private var artId: String? = null
    private var uid: String? = null
    private var username: String? = null
    private var profilePictureUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaveReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        reviewViewModel = ViewModelProvider(this)[ReviewViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        // Extract info from intent
        title = intent.getStringExtra("title")
        imageUrl = intent.getStringExtra("imageUrl")
        artId = intent.getStringExtra("artId")
        uid = intent.getStringExtra("uid")
        uid?.let { userViewModel.fetchUserByUid(it) }
        getUsernameAndProfilePicture()

        // load image
        val artImage = binding.artworkReviewImage
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.default_image)
            .into(artImage)

        // button listeners
        binding.backButtonReview.setOnClickListener {
            finish()
        }
        binding.cancelReviewButton.setOnClickListener {
            finish()
        }
        // save review or redirect to sign in page
        binding.submitReviewButton.setOnClickListener {
            if (verifyFields()) {
                saveReview()
                finish()
            }
        }
    }

    private fun getUsernameAndProfilePicture() {
        userViewModel.user.observe(this) {
            username = it?.username
            profilePictureUrl = it?.profilePictureUrl
        }
    }

    private fun saveReview() {
        val review = Review (
            title,
            binding.longEditText.text.toString(),
            binding.reviewRatingBar.rating,
            artId,
            uid,
            username,
            profilePictureUrl
        )

        reviewViewModel.saveReview(review)
        Toast.makeText(this, "Review submitted", Toast.LENGTH_SHORT).show()
    }

    // check that the user filled out all the fields
    private fun verifyFields(): Boolean {
        if (binding.longEditText.text.toString().isBlank()) {
            binding.longEditText.error = "Review is required"
            return false
        }
        if (binding.reviewRatingBar.rating <= 0) {
            Toast.makeText(this, "Rating of 1-5 is required", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}