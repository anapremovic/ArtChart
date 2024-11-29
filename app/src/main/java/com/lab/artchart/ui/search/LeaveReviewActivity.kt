package com.lab.artchart.ui.search

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.lab.artchart.R
import com.squareup.picasso.Picasso

class LeaveReviewActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leave_review)

        val imageUrl = intent.getStringExtra("imageUrl")

        val artImage = findViewById<ImageView>(R.id.artwork_review_image)
        Picasso.get().load(imageUrl).into(artImage)

        val backButton = findViewById<LinearLayout>(R.id.back_button_review)
        backButton.setOnClickListener {
            finish()
        }

        val cancelReviewButton = findViewById<Button>(R.id.cancel_review_button)
        cancelReviewButton.setOnClickListener {
            finish()
        }

        val submitReviewButton = findViewById<Button>(R.id.submit_review_button)
        submitReviewButton.setOnClickListener {
            //TODO: Send review to database
        }
    }
}