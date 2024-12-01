package com.lab.artchart.ui.search

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.lab.artchart.database.Review
import com.lab.artchart.databinding.ActivityViewReviewBinding

class ViewReviewsActivity: AppCompatActivity() {

    private lateinit var binding: ActivityViewReviewBinding

    // List views
    private lateinit var artListView: ListView
    private lateinit var arrayList: ArrayList<Review>
    private lateinit var arrayAdapter: ReviewListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        println("ArtChart1: opened")

        binding.backButtonViewReview.setOnClickListener {
            finish()
        }

        val artId = intent.getStringExtra("artId")

        artListView = binding.reviewListView

        arrayList = ArrayList()
        arrayAdapter = ReviewListAdapter(this, arrayList)
        artListView.adapter = arrayAdapter

        // TODO: Set up the reviews using the database call and put them in the adapter using arrayAdapter.replace(the list)
    }
}