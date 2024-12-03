package com.lab.artchart.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.lab.artchart.databinding.FragmentHomeBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.lab.artchart.ui.MainActivity
import com.lab.artchart.R
import com.lab.artchart.database.Artwork
import com.lab.artchart.database.ArtworkViewModel
import com.lab.artchart.service.LocationViewModel
import com.lab.artchart.ui.search.ArtInfoActivity

class HomeFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentHomeBinding? = null

    private lateinit var mMap: GoogleMap
    private lateinit var  markerOptions: MarkerOptions
    private var mapCentered = false // flag to check if map already centered to user's location
    private var googleMapsMyLocationEnabled = false // flag to check if google maps sdk location tracker already enabled

    private val artworkMarkersByArtId = mutableMapOf<String, Marker>()
    private lateinit var artworkViewModel: ArtworkViewModel
    private lateinit var locationViewModel: LocationViewModel

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view: View = binding.root

        // initialize map view and set map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    // configure map object and get user permission
    // called on create when map is ready
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        markerOptions = MarkerOptions()
        artworkViewModel = (activity as MainActivity).artworkViewModel
        locationViewModel = (activity as MainActivity).locationViewModel

        artworkViewModel.allArtworks.observe(viewLifecycleOwner) { updatedArtworks ->
            // remove markers for artworks no longer in the list
            val currentArtworkIds = updatedArtworks.map { it.artId }.toSet()
            val markersToRemove = artworkMarkersByArtId.filterKeys { it !in currentArtworkIds }
            for ((id, marker) in markersToRemove) {
                marker.remove() // removes the marker from the map
                artworkMarkersByArtId.remove(id) // removes it from the tracking map
            }

            // add new markers for artworks in the list
            for (artwork in updatedArtworks) {
                if (!artworkMarkersByArtId.containsKey(artwork.artId) && artwork.latitude != null && artwork.longitude != null) {
                    addArtMarker(artwork)
                }
            }
        }

        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(requireContext()))

        //opens art info when clicking on info window
        mMap.setOnInfoWindowClickListener { marker ->
            val artwork = marker.tag as Artwork
            val intent = Intent(requireContext(), ArtInfoActivity::class.java)
            intent.putExtra("title", artwork.title)
            intent.putExtra("artistName", artwork.artistName)
            intent.putExtra("creationYear", artwork.creationYear) // int
            intent.putExtra("latitude", artwork.latitude) // dbl
            intent.putExtra("longitude", artwork.longitude) // dbl
            intent.putExtra("description", artwork.description)
            intent.putExtra("imageUrl", artwork.imageUrl)
            intent.putExtra("artId", artwork.artId)
            startActivity(intent)
        }

        // zoom camera and add user location marker
        observeUserLocation()
    }

    private fun observeUserLocation() {
        locationViewModel.userLocation.observe(viewLifecycleOwner) {
            if (!googleMapsMyLocationEnabled) {
                enableGoogleMapsSdkLocationMarker()
            }

            if (!mapCentered) {
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(it, 15f)
                mMap.animateCamera(cameraUpdate)
                mapCentered = true
            }
        }
    }

    // show built in google maps sdk marker
    private fun enableGoogleMapsSdkLocationMarker() {
        try {
            mMap.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            Log.e("HOME_FRAG", "Security error when enabling google maps isMyLocationEnabled: $e")
            Toast.makeText(requireActivity(), "Allow location services to track your workouts", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addArtMarker(artwork: Artwork) {
        if (artwork.latitude != null && artwork.longitude != null && artwork.title != null) {
            val latLng = LatLng(artwork.latitude, artwork.longitude)

            val artMarkerOptions = MarkerOptions()
                                    .position(latLng)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))

            val marker = mMap.addMarker(artMarkerOptions)
            if (marker != null) {
                marker.tag = artwork
                artworkMarkersByArtId[artwork.artId!!] = marker // track marker in map
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
