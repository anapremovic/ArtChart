package com.lab.artchart.ui.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.lab.artchart.R
import com.lab.artchart.database.Artwork
import com.squareup.picasso.Picasso

class ArtworkAdapter(private val context: Context, private var artworks: List<Artwork>) : BaseAdapter() {
    override fun getCount(): Int = artworks.size

    override fun getItem(position: Int): Any = artworks[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_artwork, parent, false)

        val artwork = artworks[position]
        val imageView = view.findViewById<ImageView>(R.id.artwork_image)

        // set views
        view.findViewById<TextView>(R.id.title).text = artwork.title
        view.findViewById<TextView>(R.id.artist_name).text = artwork.artistName
        view.findViewById<TextView>(R.id.year).text = artwork.creationYear.toString()
        view.findViewById<TextView>(R.id.latitude).text = artwork.latitude.toString()
        view.findViewById<TextView>(R.id.longitude).text = artwork.longitude.toString()
        view.findViewById<TextView>(R.id.description).text = artwork.description
        // Picasso handles async image loading
        Picasso.get().load(artwork.imageUrl).into(imageView)

        return view
    }

    fun replace(newArtworkList: List<Artwork>) {
        artworks = newArtworkList
    }
}