package com.lab.artchart.ui.search

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.lab.artchart.CustomMapFragment
import com.lab.artchart.R
import com.lab.artchart.database.ArtworkViewModel
import com.lab.artchart.database.ReviewViewModel
import com.lab.artchart.database.UserAuthenticationViewModel
import com.lab.artchart.database.UserAuthenticationViewModelFactory
import com.lab.artchart.database.UserViewModel
import com.lab.artchart.databinding.ActivityArtInfoBinding
import com.lab.artchart.util.UserAuthenticationUtils

class ArtInfoActivity: AppCompatActivity(), OnMapReadyCallback  {
    private lateinit var binding: ActivityArtInfoBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var userAuthenticationViewModel: UserAuthenticationViewModel
    private lateinit var artworkViewModel: ArtworkViewModel
    private lateinit var reviewViewModel: ReviewViewModel

    // Art variables
    private var title: String? = ""
    private var artistName: String? = ""
    private var creationYear: Int? = 0
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0
    private var description: String? = ""
    private var imageUrl: String? = ""
    private var artId: String? = ""

    // Map
    private lateinit var artMap: GoogleMap
    private lateinit var markerOptions: MarkerOptions

    override fun onResume() {
        super.onResume()
        reviewViewModel.loadArtworkStatsByArtId()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        val userAuthenticationViewModelFactory = UserAuthenticationViewModelFactory(userViewModel)
        userAuthenticationViewModel = ViewModelProvider(this, userAuthenticationViewModelFactory)[UserAuthenticationViewModel::class.java]
        artworkViewModel = ViewModelProvider(this)[ArtworkViewModel::class.java]
        reviewViewModel = ViewModelProvider(this)[ReviewViewModel::class.java]

        // Extract info from intent
        title = intent.getStringExtra("title")
        artistName = intent.getStringExtra("artistName")
        creationYear = intent.getIntExtra("creationYear", 1900)
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        description = intent.getStringExtra("description")
        imageUrl = intent.getStringExtra("imageUrl")
        artId = intent.getStringExtra("artId")

        // query database for average rating and num reviews and update UI
        setAverageRatingAndNumReviews()

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.reportArtButton.setOnClickListener{
            artworkViewModel.toggleDetectArtField(artId!!)
            finish()
        }
        // open leave review screen or redirect to sign in screen
        binding.leaveReviewButton.setOnClickListener {
            userAuthenticationViewModel.currentUser.observe(this) { user ->
                if (user != null) {
                    val intent = Intent(this, LeaveReviewActivity::class.java)
                    intent.putExtra("title", title)
                    intent.putExtra("imageUrl", imageUrl)
                    intent.putExtra("artId", artId)
                    intent.putExtra("uid", user.uid)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Please sign in to leave a review", Toast.LENGTH_LONG).show()
                    UserAuthenticationUtils.navigateToSignInScreenFromActivity(this)
                    finish()
                }

                userAuthenticationViewModel.currentUser.removeObservers(this)
            }
        }

        binding.viewReviewButton.setOnClickListener {
            val intent = Intent(this, ViewReviewsActivity::class.java)
            intent.putExtra("artId", artId)
            intent.putExtra("title", title)
            startActivity(intent)
        }

        // load images async using Glide
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.default_image)
            .into(binding.backgroundArtworkImage)
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.default_image)
            .into(binding.artworkImage)

        binding.artworkTitle.text = title
        binding.artworkArtistAndDate.text = getString(R.string.artist_date_format, artistName, creationYear.toString())
        binding.artworkDescription.text = description

        val mSupportMapFragment: CustomMapFragment =
            supportFragmentManager.findFragmentById(R.id.artwork_map) as CustomMapFragment
        mSupportMapFragment.setListener(object :
            CustomMapFragment.OnTouchListener {
            override fun onTouch() {
                binding.nestedScrollView.requestDisallowInterceptTouchEvent(true)
            }
        })

        mSupportMapFragment.getMapAsync(this)
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

    // update info UI
    private fun setAverageRatingAndNumReviews() {
        reviewViewModel.loadArtworkStatsByArtId()
        reviewViewModel.artworkStatsByArtId.observe(this) {
            val artworkStats = it[artId]
            binding.infoRatingBar.rating = artworkStats?.averageRating ?: 0f
            binding.infoTotalReviews.text = getString(R.string.total_reviews_format, (artworkStats?.reviewCount ?: 0).toString())
        }
    }
}