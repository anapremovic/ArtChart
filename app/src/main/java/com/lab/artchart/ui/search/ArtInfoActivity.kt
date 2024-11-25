package com.lab.artchart.ui.search

import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet.Motion
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.lab.artchart.R
import com.squareup.picasso.Picasso

class ArtInfoActivity: AppCompatActivity(), OnMapReadyCallback  {

    // Art variables
    private var title: String? = ""
    private var artistName: String? = ""
    private var creationYear: Int? = 0
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0
    private var description: String? = ""
    private var imageUrl: String? = ""

    // Map
    private lateinit var artMap: GoogleMap
    private lateinit var markerOptions: MarkerOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_art_info)

        // Ectract info from intent
        title = intent.getStringExtra("title")
        artistName = intent.getStringExtra("artistName")
        creationYear = intent.getIntExtra("creationYear", 1900)
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        description = intent.getStringExtra("description")
        imageUrl = intent.getStringExtra("imageUrl")

        // Get all the necessary fields from the view
        val artImage = findViewById<ImageView>(R.id.artwork_image)
        val titleText = findViewById<TextView>(R.id.artwork_title)
        val artistDateText = findViewById<TextView>(R.id.artwork_artist_and_date)
        val descriptionText = findViewById<TextView>(R.id.artwork_description)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        Picasso.get().load(imageUrl).into(artImage)
        titleText.text = title
        artistDateText.text = "${artistName}, ${creationYear}"
        descriptionText.text = description

        val mapContainer = findViewById<FrameLayout>(R.id.map_container)
        mapContainer.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    // Tell the parent (ScrollView or NestedScrollView) not to intercept touch events
                    mapContainer.parent.parent.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_UP -> {
                    // Allow parent to intercept touch events again
                    mapContainer.parent.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false // Pass the event to the map for further handling
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.artwork_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // Set the map
    override fun onMapReady(googleMap: GoogleMap) {
        artMap = googleMap
        markerOptions = MarkerOptions()
        val artLatLng = LatLng(latitude!!, longitude!!)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(artLatLng, 15f)
        artMap.animateCamera(cameraUpdate)
        markerOptions.position(artLatLng)
        artMap.addMarker(markerOptions)
    }
}