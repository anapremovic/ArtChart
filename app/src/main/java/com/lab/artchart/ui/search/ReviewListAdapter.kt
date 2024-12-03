package com.lab.artchart.ui.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RatingBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.lab.artchart.R
import com.lab.artchart.database.Review
import com.lab.artchart.database.User

class ReviewListAdapter(
    private val context: Context,
    private var reviewList: List<Review>,
    private var usersByUid: Map<String, User>) : BaseAdapter() {
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
        val user = usersByUid[review.uid]

        // set views
        view.findViewById<TextView>(R.id.art_title_review).text = review.artworkTitle
        view.findViewById<RatingBar>(R.id.review_rating_bar).rating = review.rating!!
        view.findViewById<TextView>(R.id.review_text).text = review.comment
        view.findViewById<TextView>(R.id.username_text).text = user?.username ?: "n/a"

        // load image async using Glide
        if (user != null && user.profilePictureUrl.isNotEmpty()) {
            Glide.with(context)
                .load(user.profilePictureUrl)
                .circleCrop()
                .into(view.findViewById(R.id.user_profile_image))
        }

        return view
    }

    fun replaceReviewList(newReviewList: List<Review>) {
        reviewList = newReviewList
    }

    fun replaceUserMap(newUserMap: Map<String, User>) {
        usersByUid = newUserMap
    }
}