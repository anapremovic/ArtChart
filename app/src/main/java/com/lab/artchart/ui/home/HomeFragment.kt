package com.lab.artchart.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.LOCATION_SERVICE
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
import com.google.common.io.Resources
import com.lab.artchart.MainActivity
import com.lab.artchart.R
import com.lab.artchart.database.Artwork
import com.lab.artchart.database.FirebaseViewModel
import com.lab.artchart.ui.search.ArtInfoActivity

class HomeFragment : Fragment(), OnMapReadyCallback, LocationListener{//, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {
    private var _binding: FragmentHomeBinding? = null

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var  markerOptions: MarkerOptions
    private var mapCentered = false // flag to check if map already centered to user's location

    private lateinit var artworkList:List<Artwork>
    private lateinit var firebaseViewModel: FirebaseViewModel

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    // launcher for location permission
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            initLocationManager()
        }
        else{
            Toast.makeText(requireContext(), "Location services needed to find art near you", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

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
//        mMap.setOnMapClickListener(this)
//        mMap.setOnMapLongClickListener(this)
        markerOptions = MarkerOptions()

        firebaseViewModel = (requireActivity() as MainActivity).firebaseViewModel
        firebaseViewModel.allArtworks.observe(viewLifecycleOwner, Observer { it ->
            artworkList = it
            //Adds all artwork markers
            for (artwork in artworkList){
                addArtMarker(artwork)
            }
        })

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

    // initialize location manager if location permission already granted or open launch
    private fun getLocationPermissionOrConfigureLocationManager() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initLocationManager()
            //sets the user's current location
            mMap.isMyLocationEnabled = true
        }
        else{
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // configure location manager system service
    private fun initLocationManager() {
        try {
            locationManager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager

            // check if GPS enabled on device
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.w("HOME_FRAG", "GPS not enabled, cannot use location")
                Toast.makeText(requireContext(), "Enable GPS on your device to find art near you", Toast.LENGTH_SHORT).show()
                return
            }

            // update camera based on last known location
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                onLocationChanged(location)
            }

            // request location updates from GPS
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5f, this)
        } catch (e: SecurityException) {
            Log.e("HOME_FRAG", "Security error when initializing location manager: $e")
            Toast.makeText(requireContext(), "Allow location services to find art near you", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onLocationChanged(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)

        // update camera to user's location and add a marker
        if (!mapCentered) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
            mMap.animateCamera(cameraUpdate)
            mapCentered = true
        }
    }

    private fun addArtMarker(artwork: Artwork){
        if (artwork.latitude != null && artwork.longitude!=null && artwork.title != null) {
            val latlng = LatLng(artwork.latitude, artwork.longitude)

            val artMarkerOptions = MarkerOptions()
            artMarkerOptions.position(latlng)
            artMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))

            val marker = mMap.addMarker(artMarkerOptions)
            if (marker != null) {
                marker.tag = artwork
            }
        }
    }

//    override fun onMapClick(latLng: LatLng) {
//        Log.d("HOME_FRAG", "Map short clicked ${latLng.latitude}, ${latLng.longitude}")
//    }
//
//    override fun onMapLongClick(latLng: LatLng) {
//        Log.d("HOME_FRAG", "Map long clicked ${latLng.latitude}, ${latLng.longitude}")
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
