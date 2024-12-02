package com.lab.artchart.ui.home

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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

//        loads image
//        if (artwork.imageUrl != null){
//            Glide.with(context)
//                .load(artwork.imageUrl)
//                .placeholder(R.drawable.default_image)
//                .listener(MarkerCallback(marker))
//                .into(img)
//        }
        Picasso.get().load(artwork.imageUrl).into(img, object:com.squareup.picasso.Callback{
            override fun onSuccess() { //image successfully loaded
//                println("IMAGE LOADED")
                //refresh the info window to show image
                if (marker.isInfoWindowShown){
                    marker.showInfoWindow()
                }
            }
            override fun onError(e: Exception?) {
//                println("ERROR")
            }
        })

        return view
    }

    inner class MarkerCallback(marker: Marker?) : RequestListener<Drawable>{
        private var marker:Marker? = null

        init {
            this.marker = marker
        }

        private fun onSuccess(){
//            println("LOADED IMAGE")
            if (marker!=null && marker!!.isInfoWindowShown){
                marker!!.hideInfoWindow()
                marker!!.showInfoWindow()
            }
        }

        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
//            println("ERROR LOADING IMAGE")
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            onSuccess()
            return false
        }

    }
}