package com.lab.artchart.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.lab.artchart.R
import com.lab.artchart.database.Artwork
import com.squareup.picasso.Picasso
import java.lang.Exception

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

        //loads image
        Picasso.get().load(artwork.imageUrl).into(img, object:com.squareup.picasso.Callback{
            override fun onSuccess() { //image successfully loaded
                //refresh the info window to show image
                if (marker.isInfoWindowShown){
                    marker.showInfoWindow()
                }
            }
            override fun onError(e: Exception?) { }
        })

        return view
    }
}