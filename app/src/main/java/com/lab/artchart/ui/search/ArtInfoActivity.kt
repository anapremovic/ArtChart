package com.lab.artchart.ui.search

// import android.R
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.lab.artchart.CustomMapFragment
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

        val scrollView = findViewById<androidx.core.widget.NestedScrollView>(R.id.nestedScrollView)
        val mSupportMapFragment: CustomMapFragment =
            supportFragmentManager.findFragmentById(R.id.artwork_map) as CustomMapFragment
        if (mSupportMapFragment != null) mSupportMapFragment.setListener(object :
            CustomMapFragment.OnTouchListener {
            override fun onTouch() {
                scrollView.requestDisallowInterceptTouchEvent(true)
            }
        })

        // val mapFragment = supportFragmentManager.findFragmentById(com.lab.artchart.R.id.artwork_map) as SupportMapFragment
        mSupportMapFragment.getMapAsync(this)
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