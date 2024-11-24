package com.lab.artchart.ui.addArt

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.LOCATION_SERVICE
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lab.artchart.ui.MainActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.lab.artchart.R
import com.lab.artchart.database.Artwork
import com.lab.artchart.database.ArtworkViewModel
import com.lab.artchart.databinding.FragmentAddArtBinding

class AddArtFragment : Fragment(), OnMapReadyCallback, LocationListener, GoogleMap.OnMapClickListener {

    // map values and variables
    private lateinit var artMap: GoogleMap
    private val PERMISSION_REQUEST_CODE = 0
    private lateinit var locationManager: LocationManager
    private var mapCentered = false
    private lateinit var  markerOptions: MarkerOptions

    private var latitude: Double? = null
    private var longitude: Double? = null

    // others
    private var _binding: FragmentAddArtBinding? = null

    private lateinit var artworkViewModel: ArtworkViewModel
    private var artworkImageUri: Uri? = null
    private lateinit var artworkImageView: ImageView

    // launcher for gallery permission
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            getImageFromGallery()
        } else {
            Toast.makeText(requireContext(), "Gallery permission needed to upload artwork image", Toast.LENGTH_SHORT).show()
        }
    }

    // launcher to handle selected image from gallery
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            artworkImageUri = result.data?.data
            artworkImageUri?.let {
                // update view
                artworkImageView.setImageURI(it)
            }
        }
    }

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val addArtViewModel = ViewModelProvider(this)[AddArtViewModel::class.java]

        _binding = FragmentAddArtBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // views
        val saveButton = binding.saveButton
        val selectPhotoButton = binding.selectPhotoButton
        artworkImageView = binding.artworkImage

        // Map stuff
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // request correct permission based on android version or open gallery if already granted
        selectPhotoButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                openGalleryOrRequestPermission(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                openGalleryOrRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        // save artwork to database
        saveButton.setOnClickListener {
            artworkViewModel = (activity as MainActivity).artworkViewModel
            if (artworkImageUri != null) {
                var roundedLat: Double? = 0.0
                if (latitude != null) {
                    roundedLat = (Math.round(latitude!! * 100) / 100.0)
                }
                else {
                    roundedLat = null
                }
                var roundedLong: Double? = 0.0
                if (longitude != null) {
                    roundedLong = (Math.round(longitude!! * 100) / 100.0)
                }
                else {
                    roundedLong = null
                }
                val testArtwork = Artwork(binding.title.text.toString(),
                    binding.artistName.text.toString(),
                    binding.year.text.toString().toIntOrNull(),
                    roundedLat,
                    roundedLong,
                    binding.description.text.toString())
                // save to firebase realtime database and firebase storage
                artworkViewModel.saveArtwork(testArtwork, artworkImageUri!!)
                Toast.makeText(requireContext(), "Saved Artwork to database", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please upload artwork image to submit", Toast.LENGTH_SHORT).show()
            }

        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // mapReady
    override fun onMapReady(map: GoogleMap) {
        artMap = map
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        artMap.setOnMapClickListener(this)
        markerOptions = MarkerOptions()
        // println("ArtChart1: about to check permissions")
        centerLocationOrCheckPermission()
    }

    override fun onLocationChanged(location: Location) {
        // println("ArtChart1: onlocationchanged() ${location.latitude} ${location.longitude}")
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
        // println("ArtChart1: clicked the map at ${latLng.latitude}, ${latLng.longitude}")
        artMap.clear()
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
        artMap.animateCamera(cameraUpdate)
        markerOptions.position(latLng)
        latitude = latLng.latitude
        longitude = latLng.longitude
        artMap.addMarker(markerOptions)
    }

    fun initLocationManager() {
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

    // open gallery if permission already granted or launch permission launcher
    private fun openGalleryOrRequestPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            getImageFromGallery()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    // launch intent to open gallery
    private fun getImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }
}