package com.lab.artchart.ui.search

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.lab.artchart.R
import com.lab.artchart.database.Artwork
import com.lab.artchart.database.ArtworkViewModel
import com.lab.artchart.database.LocationViewModel
import com.lab.artchart.ui.MainActivity
import com.lab.artchart.databinding.FragmentSearchBinding

class SearchFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: FragmentSearchBinding? = null
    private lateinit var listView: ListView
    private lateinit var currArtworkList: List<Artwork>
    private lateinit var artworkViewModel: ArtworkViewModel

    private var currentLocation: Location? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // observe remote database items and update listview
        val adapter = ArtworkAdapter(requireContext(), mutableListOf())
        listView = binding.artworkListView
        listView.adapter = adapter

        val artworkViewModel = (activity as MainActivity).artworkViewModel
        artworkViewModel.allArtworks.observe(viewLifecycleOwner) {
            currArtworkList = it
            adapter.replace(it)
            adapter.notifyDataSetChanged()
        }

        val locationViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]
        locationViewModel.location.observe(viewLifecycleOwner, Observer { it ->
            adapter.updateLocation(it)
            adapter.notifyDataSetChanged()
            currentLocation = it
        })

        listView.setOnItemClickListener { _, _, position, _ ->
            val selected = listView.adapter.getItem(position) as Artwork
            val intent = Intent(requireContext(), ArtInfoActivity::class.java)
            intent.putExtra("title", selected.title)
            intent.putExtra("artistName", selected.artistName)
            intent.putExtra("creationYear", selected.creationYear) // int
            intent.putExtra("latitude", selected.latitude) // dbl
            intent.putExtra("longitude", selected.longitude) // dbl
            intent.putExtra("description", selected.description)
            intent.putExtra("imageUrl", selected.imageUrl)
            startActivity(intent)
        }

        //SEARCH VIEW
        listView.isTextFilterEnabled = true
        val searchView = root.findViewById<SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(this)

        //NAME BUTTON - filter title alphabetically
        val nameBtn = root.findViewById<Button>(R.id.filterNameButton)
        nameBtn.setOnClickListener{
            currArtworkList = currArtworkList.sortedBy { it.title?.lowercase() }
            adapter.replace(currArtworkList)
            adapter.notifyDataSetChanged()
        }

        //YEAR BUTTON - sort by most recent year
        val yearBtn = root.findViewById<Button>(R.id.filterYearButton)
        yearBtn.setOnClickListener{
            currArtworkList = currArtworkList.sortedByDescending { it.creationYear }
            adapter.replace(currArtworkList)
            adapter.notifyDataSetChanged()
        }

        //DISTANCE BUTTON
        val distBtn = root.findViewById<Button>(R.id.filterDistanceButton)
        distBtn.setOnClickListener{
            if (currentLocation!=null){
                currArtworkList = currArtworkList.sortedBy { artwork ->

                    if (artwork.latitude==null || artwork.longitude==null){
                        Float.MAX_VALUE
                    }else{
                        val artworkLocation = Location("art").apply {
                            latitude = artwork.latitude
                            longitude = artwork.longitude
                        }
                        val dist = currentLocation!!.distanceTo(artworkLocation)
                        dist
                    }
                }

                adapter.replace(currArtworkList)
                adapter.notifyDataSetChanged()
            }
        }

        //TO DO -> REVIEW
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (TextUtils.isEmpty(newText)){
            listView.clearTextFilter();
        }else{
            listView.setFilterText(newText)
        }
        return true
    }
}