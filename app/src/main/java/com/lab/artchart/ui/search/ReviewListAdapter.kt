package com.lab.artchart.ui.search

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.lab.artchart.R
import com.lab.artchart.database.Review

class ReviewListAdapter(private val context: Context, private var reviewList: List<Review>) : BaseAdapter() {
    override fun getCount(): Int {
        return reviewList.size
    }

    override fun getItem(position: Int): Any {
        return reviewList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.layout_adapter_review, null)

        val userImage = view.findViewById<ImageView>(R.id.user_profile_image)
        val username = view.findViewById<TextView>(R.id.username)

        // TODO: Set the userImage, usernamear with user info

        val ratingBar = view.findViewById<RatingBar>(R.id.review_rating_bar)
        ratingBar.rating = reviewList[position].rating!!
        val review = view.findViewById<TextView>(R.id.review_text)
        review.text = reviewList[position].comment

        return view
    }
}