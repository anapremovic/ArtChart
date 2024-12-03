package com.lab.artchart.ui.home

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.lab.artchart.R
import com.lab.artchart.database.Artwork

class CustomInfoWindowAdapter (private val context: Context) : GoogleMap.InfoWindowAdapter{

    override fun getInfoContents(marker: Marker): View? {
        return null
    }

    override fun getInfoWindow(marker: Marker): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)
        val img:ImageView = view.findViewById(R.id.infoWindowImage)
        val title:TextView = view.findViewById(R.id.infoWindowTitle)
        val desc:TextView = view.findViewById(R.id.infoWindowDesc)

        val artwork = marker.tag as Artwork

        title.text = artwork.title
        val descString = artwork.artistName+" | "+artwork.creationYear
        desc.text = descString

        // load image to marker async using Glide
        Glide.with(context)
            .load(artwork.imageUrl)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    // set the image to the ImageView
                    img.setImageDrawable(resource)

                    // refresh the info window to show the image
                    if (marker.isInfoWindowShown) {
                        marker.showInfoWindow()
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    img.setImageDrawable(placeholder)
                }
            })

        return view
    }
}