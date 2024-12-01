package com.lab.artchart.ui.search

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.lab.artchart.R
import com.lab.artchart.database.Review

class ReviewListAdapter(private val context: Context, private var reviewList: List<Review>) : BaseAdapter() {
    override fun getCount(): Int {
        return reviewList.size
    }

    override fun getItem(position: Int): Any {
        return reviewList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.layout_adapter_review, parent, false)
        val review = reviewList[position]

        val ratingBar = view.findViewById<RatingBar>(R.id.review_rating_bar)
        ratingBar.rating = review.rating!!
        val comment = view.findViewById<TextView>(R.id.review_text)
        comment.text = review.comment
        if (!review.profilePictureUrl.isNullOrEmpty()) {
            val userImage = view.findViewById<ImageView>(R.id.user_profile_image)
            Glide.with(context)
                .load(review.profilePictureUrl)
                .circleCrop()
                .into(userImage)
        }
        val username = view.findViewById<TextView>(R.id.username_text)
        username.text = review.username

        return view
    }

    fun replace(newReviewList: List<Review>) {
        Log.d("REVIEW_LIST_ADAPTER", "Replacing with: $newReviewList")
        reviewList = newReviewList
    }
}