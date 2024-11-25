package com.lab.artchart.ui.addArt

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.lab.artchart.ui.MainActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.lab.artchart.CustomMapFragment
import com.lab.artchart.R
import com.lab.artchart.database.Artwork
import com.lab.artchart.database.ArtworkViewModel
import com.lab.artchart.databinding.FragmentAddArtBinding
import com.lab.artchart.util.ImageGalleryManager

class AddArtFragment : Fragment(), OnMapReadyCallback, LocationListener, GoogleMap.OnMapClickListener {
    // map values and variables
    private lateinit var artMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private var mapCentered = false
    private lateinit var  markerOptions: MarkerOptions
    private var latitude: Double? = null
    private var longitude: Double? = null

    // others
    private var _binding: FragmentAddArtBinding? = null

    private lateinit var artworkViewModel: ArtworkViewModel
    private lateinit var imageGalleryManager: ImageGalleryManager

    // launcher to handle selected image from gallery
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        imageGalleryManager.handleSelectedImage(result)
    }

    // launcher for gallery permission
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        imageGalleryManager.handlePermission(isGranted, requireContext(), selectImageLauncher)
    }

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddArtBinding.inflate(inflater, container, false)
        val root: View = binding.root
        artworkViewModel = (activity as MainActivity).artworkViewModel
        imageGalleryManager = ImageGalleryManager(binding.artworkImage)

        // views
        val saveButton = binding.saveButton
        val selectPhotoButton = binding.selectPhotoButton

        // Map stuff
        val scrollView = binding.nestedScrollViewAddArt
        val mSupportMapFragment: CustomMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as CustomMapFragment
        mSupportMapFragment.setListener(object :
            CustomMapFragment.OnTouchListener {
            override fun onTouch() {
                scrollView.requestDisallowInterceptTouchEvent(true)
            }
        })
        mSupportMapFragment.getMapAsync(this)

        // request correct permission based on android version or open gallery if already granted
        selectPhotoButton.setOnClickListener {
            imageGalleryManager.openGalleryOrRequestPermission(requireContext(), requestPermissionLauncher, selectImageLauncher)
        }

        // save artwork to database
        saveButton.setOnClickListener {
            if (verifyFields()) {
                val roundedLat = if (latitude != null) {
                    (Math.round(latitude!! * 100) / 100.0)
                } else {
                    null
                }
                val roundedLong = if (longitude != null) {
                    (Math.round(longitude!! * 100) / 100.0)
                } else {
                    null
                }
                val artwork = Artwork(
                    binding.title.text.toString(),
                    binding.artistName.text.toString(),
                    binding.year.text.toString().toIntOrNull(),
                    roundedLat,
                    roundedLong,
                    binding.description.text.toString(),
                    null,
                    true) // PETER MADE CHANGES HERE,
                // ADDED NULL URL AND ART IS ALWAYS TRUE
                // detectArt IS ONLY CHANGED TO FALSE BY FIREBASE TRIGGER
                // save to firebase realtime database and firebase storage
                artworkViewModel.saveArtwork(artwork, imageGalleryManager.imageUri!!)
                Toast.makeText(requireContext(), "Artwork submitted", Toast.LENGTH_SHORT).show()
                resetFragment()
            }
        }

        return root
    }

    // check that the user filled out all the fields
    private fun verifyFields(): Boolean {
        var missingTextFields = false
        if (binding.title.text.toString().isBlank()) {
            binding.title.error = "Title is required"
            missingTextFields = true
        }
        if (binding.artistName.text.toString().isBlank()) {
            binding.artistName.error = "Artist name is required"
            missingTextFields = true
        }
        if (binding.year.text.toString().isBlank()) {
            binding.year.error = "Year is required"
            missingTextFields = true
        }
        if (binding.description.text.toString().isBlank()) {
            binding.description.error = "Description is required"
            missingTextFields = true
        }

        if (imageGalleryManager.imageUri == null) {
            Toast.makeText(requireContext(), "Please upload artwork image to submit", Toast.LENGTH_SHORT).show()
            return false
        }

        if (missingTextFields) {
            Toast.makeText(requireContext(), "Missing one or more fields", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // reset fragment after submitting an artwork
    private fun resetFragment() {
        binding.title.text?.clear()
        binding.artistName.text?.clear()
        binding.year.text?.clear()
        binding.description.text?.clear()
        mapCentered = false
        binding.artworkImage.setImageResource(R.drawable.default_image)
    }

    // mapReady
    override fun onMapReady(map: GoogleMap) {
        artMap = map
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        artMap.setOnMapClickListener(this)
        markerOptions = MarkerOptions()
        centerLocationOrCheckPermission()
    }

    override fun onLocationChanged(location: Location) {
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)
        if (!mapCentered) { // add first marker when centered (will remove if user clicks a new location)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
            artMap.animateCamera(cameraUpdate)
            markerOptions.position(latLng)
            artMap.addMarker(markerOptions)
            latitude = latLng.latitude
            longitude = latLng.longitude
            mapCentered = true
        }
    }

    override fun onMapClick(latLng: LatLng) {
        artMap.clear()
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
        artMap.animateCamera(cameraUpdate)
        markerOptions.position(latLng)
        latitude = latLng.latitude
        longitude = latLng.longitude
        artMap.addMarker(markerOptions)
    }

    private fun initLocationManager() {
        try {
            locationManager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager

            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return

            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null)
                onLocationChanged(location)

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)

        } catch (e: SecurityException) {
            Log.e("ADD_ART_FRAG", "Security error when initializing location manager: $e")
            Toast.makeText(requireContext(), "Allow location services to log art near you", Toast.LENGTH_SHORT).show()
        }
    }

    private fun centerLocationOrCheckPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initLocationManager()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}