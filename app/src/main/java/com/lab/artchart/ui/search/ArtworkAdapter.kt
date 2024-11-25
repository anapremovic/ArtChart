package com.lab.artchart.ui.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.lab.artchart.R
import com.lab.artchart.database.Artwork
import com.squareup.picasso.Picasso


class ArtworkAdapter(private val context: Context, private var artworks: List<Artwork>) : BaseAdapter(), Filterable {

    private var orig: List<Artwork>? = null

    override fun getCount(): Int = artworks.size

    override fun getItem(position: Int): Any = artworks[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_artwork, parent, false)

        val artwork = artworks[position]
        val imageView = view.findViewById<ImageView>(R.id.artwork_image)

        // set views
        view.findViewById<TextView>(R.id.title).text = artwork.title
        val artistAndYearString = artwork.artistName+" | "+artwork.creationYear
        view.findViewById<TextView>(R.id.artist_name_and_year).text = artistAndYearString
        val latAndLong = artwork.latitude.toString()+", "+artwork.longitude.toString()
        view.findViewById<TextView>(R.id.distance).text = latAndLong
        //TO DO: Set RATING
        view.findViewById<RatingBar>(R.id.rating_bar_search).rating = 2.5f
//        view.findViewById<TextView>(R.id.artist_name).text = artwork.artistName
//        view.findViewById<TextView>(R.id.year).text = artwork.creationYear.toString()
//        view.findViewById<TextView>(R.id.latitude).text = artwork.latitude.toString()
//        view.findViewById<TextView>(R.id.longitude).text = artwork.longitude.toString()
//        view.findViewById<TextView>(R.id.description).text = artwork.description
        // Picasso handles async image loading
        Picasso.get().load(artwork.imageUrl).into(imageView)

        return view
    }

    fun replace(newArtworkList: List<Artwork>) {
        artworks = newArtworkList
    }

    override fun getFilter(): Filter {
        //returns new filter object
        return object :Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                //returned object
                val results = FilterResults()
                val filteredList : MutableList<Artwork> = mutableListOf()

                //if list is empty, fill
                if (orig == null){
                    orig = artworks
                }

                //filter through
                if (!constraint.isNullOrEmpty() && orig != null){
                    for (artwork in orig!!){
                        if (artwork.title?.lowercase()?.contains(constraint.toString().lowercase()) == true){
                            filteredList.add(artwork)
                        }
                    }
                    results.values = filteredList
                    results.count = filteredList.size
                //if there's nothing, show all
                }else{
                    results.values = orig
                    results.count = orig!!.size
                }

                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                //update the results
                if (results != null) {
                    artworks = results.values as List<Artwork>
                }
                notifyDataSetChanged()
            }

        }
    }
}