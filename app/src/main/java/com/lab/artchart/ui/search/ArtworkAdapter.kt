package com.lab.artchart.ui.search

import android.content.Context
import android.location.Location
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
import com.lab.artchart.database.ArtworkStats
import com.squareup.picasso.Picasso


class ArtworkAdapter(private val context: Context,
                     private var artworks: List<Artwork>,
                     private var artworkStatsByArtwork: Map<String, ArtworkStats>) : BaseAdapter(), Filterable {
    private var orig: List<Artwork>? = null
    private var currentLocation: Location? = null

    override fun getCount(): Int = artworks.size

    override fun getItem(position: Int): Any = artworks[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.layout_adapter_artwork, parent, false)

        val artwork = artworks[position]
        val artworkStats = artworkStatsByArtwork[artwork.artId]

        // set views
        view.findViewById<TextView>(R.id.title).text = artwork.title
        val artistAndYearString = artwork.artistName+" | "+artwork.creationYear
        view.findViewById<TextView>(R.id.artist_name_and_year).text = artistAndYearString
        setLocation(artwork, view)
        view.findViewById<RatingBar>(R.id.rating_bar_search).rating = artworkStats?.averageRating ?: 0f
        view.findViewById<TextView>(R.id.total_reviews).text = context.getString(R.string.total_reviews_format, (artworkStats?.reviewCount ?: 0).toString())
        // Picasso handles async image loading
        Picasso.get().load(artwork.imageUrl).into(view.findViewById<ImageView>(R.id.artwork_image))
        return view
    }

    private fun setLocation(artwork: Artwork, view: View) {
        if (artwork.latitude!=null && artwork.longitude!=null && currentLocation!=null){
            val artworkLocation = Location("art").apply {
                latitude = artwork.latitude
                longitude = artwork.longitude
            }

            var dist = currentLocation!!.distanceTo(artworkLocation).toInt()
            var unitString = "m"

            //if more then a thousand, convert to km
            if (dist>=1000){
                dist /= 1000
                unitString = "km"
            }
            val distance = "$dist $unitString"

            view.findViewById<TextView>(R.id.distance).text = distance
        }
    }

    fun replaceArtworkList(newArtworkList: List<Artwork>) {
        artworks = newArtworkList
    }

    fun replaceArtworkStats(newArtworkStatsByArtwork: Map<String, ArtworkStats>) {
        artworkStatsByArtwork = newArtworkStatsByArtwork
    }

    fun updateLocation(location: Location){
        currentLocation = location
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