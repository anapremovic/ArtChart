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
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.lab.artchart.R
import com.lab.artchart.database.Artwork
import com.lab.artchart.database.LocationViewModel
import com.squareup.picasso.Picasso


class ArtworkAdapter(private val context: Context, private var artworks: List<Artwork>) : BaseAdapter(), Filterable {

    private var orig: List<Artwork>? = null
    private var currentLocation: Location? = null

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

        var latAndLong = ""
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
            latAndLong = "$dist $unitString"
        }

        view.findViewById<TextView>(R.id.distance).text = latAndLong
        //TO DO: Set RATING
        // Picasso handles async image loading
        Picasso.get().load(artwork.imageUrl).into(imageView)
        return view
    }

    fun replace(newArtworkList: List<Artwork>) {
        artworks = newArtworkList
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