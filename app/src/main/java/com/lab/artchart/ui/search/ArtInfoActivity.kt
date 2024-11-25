package com.lab.artchart.ui.search

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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

        // Extract info from intent
        title = intent.getStringExtra("title")
        artistName = intent.getStringExtra("artistName")
        creationYear = intent.getIntExtra("creationYear", 1900)
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        description = intent.getStringExtra("description")
        imageUrl = intent.getStringExtra("imageUrl")

        // Get all the necessary fields from the view
        val backgroundArtImage = findViewById<ImageView>(R.id.background_artwork_image)
        val artImage = findViewById<ImageView>(R.id.artwork_image)
        val titleText = findViewById<TextView>(R.id.artwork_title)
        val artistDateText = findViewById<TextView>(R.id.artwork_artist_and_date)
        val descriptionText = findViewById<TextView>(R.id.artwork_description)

        val backButton = findViewById<LinearLayout>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        Picasso.get().load(imageUrl).into(backgroundArtImage)
        Picasso.get().load(imageUrl).into(artImage)
        titleText.text = title
        artistDateText.text = getString(R.string.artist_date_format, artistName, creationYear.toString())
        //artistDateText.text = "${artistName} | ${creationYear}"
        descriptionText.text = description


        val mapFragment = supportFragmentManager.findFragmentById(R.id.artwork_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // Set the map
    override fun onMapReady(googleMap: GoogleMap) {
        artMap = googleMap
        markerOptions = MarkerOptions()
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        val artLatLng = LatLng(latitude!!, longitude!!)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(artLatLng, 15f)
        artMap.animateCamera(cameraUpdate)
        markerOptions.position(artLatLng)
        artMap.addMarker(markerOptions)
    }
}