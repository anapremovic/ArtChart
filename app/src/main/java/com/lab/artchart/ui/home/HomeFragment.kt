package com.lab.artchart.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.LOCATION_SERVICE
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lab.artchart.databinding.FragmentHomeBinding
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.lab.artchart.R

class HomeFragment : Fragment(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    private var _binding: FragmentHomeBinding? = null

    private lateinit var mMap: GoogleMap

    private val PERMISSION_REQUEST_CODE = 0
    private lateinit var locationManager: LocationManager
    private var mapCentered = false
    private lateinit var  markerOptions: MarkerOptions

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            initLocationManager()
        }
        else{
            Toast.makeText(requireContext(), "Gallery permission needed to use map", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)


        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view: View = binding.root

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        // println("ArtChart1: Test")
        mapFragment.getMapAsync(this)

        return view
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setOnMapClickListener(this)
        mMap.setOnMapLongClickListener(this)
        markerOptions = MarkerOptions()

        checkPermission()
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
        }
    }

    override fun onLocationChanged(location: Location) {
        println("ArtChart1: onlocationchanged() ${location.latitude} ${location.longitude}")
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)
        if (!mapCentered) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
            mMap.animateCamera(cameraUpdate)
            markerOptions.position(latLng)
            mMap.addMarker(markerOptions)
            mapCentered = true
        }
    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initLocationManager()
        }
        else{
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onMapClick(latLng: LatLng) {
        println("ArtChart1: map clicked ${latLng.latitude}, ${latLng.longitude}")
    }

    override fun onMapLongClick(latLng: LatLng) {
        println("ArtChart1: map clicked long ${latLng.latitude}, ${latLng.longitude}")
    }

    /*
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) initLocationManager()
        }
    }
     */

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
