package com.lab.artchart.ui.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lab.artchart.databinding.ActivityLeaveReviewBinding
import com.squareup.picasso.Picasso

class LeaveReviewActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLeaveReviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaveReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Extract info from intent
        imageUrl = intent.getStringExtra("imageUrl")
        artId = intent.getStringExtra("artId")
        uid = intent.getStringExtra("uid")

        // load image
        val artImage = binding.artworkReviewImage
        Picasso.get().load(imageUrl).into(artImage)

        binding.backButtonReview.setOnClickListener {
            finish()
        }

        binding.cancelReviewButton.setOnClickListener {
            finish()
        }

        binding.submitReviewButton.setOnClickListener {
            //TODO: Send review to database
        }
    }
}