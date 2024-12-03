package com.lab.artchart.ui.search

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.lab.artchart.R
import com.lab.artchart.database.Artwork
import com.lab.artchart.database.ArtworkStats
import com.lab.artchart.database.ArtworkViewModel
import com.lab.artchart.database.ReviewViewModel
import com.lab.artchart.ui.MainActivity
import com.lab.artchart.databinding.FragmentSearchBinding
import com.lab.artchart.service.LocationViewModel

class SearchFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: FragmentSearchBinding? = null
    private lateinit var listView: ListView
    private lateinit var adapter: ArtworkAdapter
    private lateinit var artworkViewModel: ArtworkViewModel
    private lateinit var reviewViewModel: ReviewViewModel
    private lateinit var locationViewModel: LocationViewModel
    private var currArtworkList = listOf<Artwork>()
    private var artworkStatsByArtId = mapOf<String, ArtworkStats>()
    private var userLocation: Location? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onResume() {
        super.onResume()
        reviewViewModel.loadArtworkStatsByArtId()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root
        artworkViewModel = (activity as MainActivity).artworkViewModel
        reviewViewModel = (activity as MainActivity).reviewViewModel
        locationViewModel = (activity as MainActivity).locationViewModel

        // update location data
        observeUserLocation()

        // load review stats
        reviewViewModel.loadArtworkStatsByArtId()

        // observe remote database items and update listview
        adapter = ArtworkAdapter(requireContext(), mutableListOf(), mapOf())
        listView = binding.artworkListView
        listView.adapter = adapter
        artworkViewModel.allArtworks.observe(viewLifecycleOwner) {
            currArtworkList = it
            adapter.replaceArtworkList(it)
            adapter.notifyDataSetChanged()
        }
        reviewViewModel.artworkStatsByArtId.observe(viewLifecycleOwner) {
            artworkStatsByArtId = it
            adapter.replaceArtworkStats(it)
            adapter.notifyDataSetChanged()
        }

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
            intent.putExtra("artId", selected.artId)
            startActivity(intent)
        }

        //SEARCH VIEW
        listView.isTextFilterEnabled = true
        val searchView = root.findViewById<SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(this)

        //NAME BUTTON - filter title alphabetically
        binding.filterNameButton.setOnClickListener{
            currArtworkList = currArtworkList.sortedBy { it.title?.lowercase() }
            adapter.replaceArtworkList(currArtworkList)
            adapter.notifyDataSetChanged()
        }

        //YEAR BUTTON - sort by most recent year
        binding.filterYearButton.setOnClickListener{
            currArtworkList = currArtworkList.sortedByDescending { it.creationYear }
            adapter.replaceArtworkList(currArtworkList)
            adapter.notifyDataSetChanged()
        }

        // DISTANCE BUTTON - sort by distance to user
        binding.filterDistanceButton.setOnClickListener {
            currArtworkList = currArtworkList.sortedBy { artwork ->
                if (artwork.latitude == null || artwork.longitude == null) {
                    Float.MAX_VALUE
                } else {
                    val artworkLocation = Location("art").apply {
                        latitude = artwork.latitude
                        longitude = artwork.longitude
                    }

                    val dist = userLocation?.distanceTo(artworkLocation)
                    dist
                }
            }

            adapter.replaceArtworkList(currArtworkList)
            adapter.notifyDataSetChanged()
        }

        // RATING - sort by highest rating
        binding.filterRatingsButton.setOnClickListener {
            currArtworkList = currArtworkList.sortedByDescending { artwork ->
                val rating = artworkStatsByArtId[artwork.artId]?.averageRating ?: 0f
                rating
            }

            adapter.replaceArtworkList(currArtworkList)
            adapter.notifyDataSetChanged()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeUserLocation() {
        locationViewModel.userLocation.observe(viewLifecycleOwner) { userLatLng ->
            userLocation = Location("user").apply {
                latitude = userLatLng.latitude
                longitude = userLatLng.longitude
            }

            adapter.updateLocation(userLocation!!)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (TextUtils.isEmpty(newText)){
            listView.clearTextFilter()
        }else{
            listView.setFilterText(newText)
        }
        return true
    }
}