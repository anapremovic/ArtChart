package com.lab.artchart.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.lab.artchart.databinding.FragmentHomeBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.lab.artchart.ui.MainActivity
import com.lab.artchart.R
import com.lab.artchart.database.Artwork
import com.lab.artchart.database.ArtworkViewModel
import com.lab.artchart.database.LocationViewModel
import com.lab.artchart.ui.search.ArtInfoActivity
import com.lab.artchart.database.LocationService

class HomeFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentHomeBinding? = null

    private lateinit var mMap: GoogleMap
//    private lateinit var locationManager: LocationManager
    private lateinit var  markerOptions: MarkerOptions
    private var mapCentered = false // flag to check if map already centered to user's location

    private lateinit var artworkList:List<Artwork>
    private lateinit var artworkViewModel: ArtworkViewModel

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    //Location Service
    private var isBind = false
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var locationIntent: Intent
    private lateinit var backPressedCallback: OnBackPressedCallback

    // launcher for location permission
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
//            initLocationManager()
            requireContext().startService(locationIntent)
            bindService()
        }
        else{
            Toast.makeText(requireContext(), "Location services needed to find art near you", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view: View = binding.root

        // initialize map view and set map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationIntent = Intent(requireContext(), LocationService::class.java)

        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]
        locationViewModel.location.observe(viewLifecycleOwner, Observer { it ->
//            println("updated location: "+it);
            updateMap(it)
        })

        //Back Pressed
        backPressedCallback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                unBindService()
                requireContext().stopService(locationIntent)
                isEnabled = false;
            }
        }

        return view
    }

    // configure map object and get user permission
    // called on create when map is ready
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        markerOptions = MarkerOptions()

        artworkViewModel = (requireActivity() as MainActivity).artworkViewModel
        artworkViewModel.allArtworks.observe(viewLifecycleOwner) {
            artworkList = it
            //Adds all artwork markers
            for (artwork in artworkList){
                addArtMarker(artwork)
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
            startActivity(intent)
        }

        getLocationPermissionOrConfigureLocationManager()
    }

    private fun updateMap(location: Location){
        val latLng = LatLng(location.latitude, location.longitude)

        // update camera to user's location
        if (!mapCentered) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
            mMap.animateCamera(cameraUpdate)
            mapCentered = true
        }
    }

    private fun bindService(){
        if (!isBind){
            requireContext().bindService(locationIntent, locationViewModel, Context.BIND_AUTO_CREATE)
            isBind = true
        }
    }

    private fun unBindService(){
        if (isBind){
            requireContext().unbindService(locationViewModel)
            isBind = false;
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        backPressedCallback.remove()
    }

    // initialize location manager if location permission already granted or open launch
    private fun getLocationPermissionOrConfigureLocationManager() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            initLocationManager()
            requireContext().startService(locationIntent)
            bindService()
            //sets the user's current location
            mMap.isMyLocationEnabled = true
        }
        else{
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun addArtMarker(artwork: Artwork){
        if (artwork.latitude != null && artwork.longitude!=null && artwork.title != null) {
            val latLng = LatLng(artwork.latitude, artwork.longitude)

            val artMarkerOptions = MarkerOptions()
            artMarkerOptions.position(latLng)
            artMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))

            val marker = mMap.addMarker(artMarkerOptions)
            if (marker != null) {
                marker.tag = artwork
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
