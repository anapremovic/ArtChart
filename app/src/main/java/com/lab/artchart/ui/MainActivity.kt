package com.lab.artchart.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseUser
import com.lab.artchart.R
import com.lab.artchart.database.ArtworkViewModel
import com.lab.artchart.database.ReviewViewModel
import com.lab.artchart.database.UserAuthenticationViewModel
import com.lab.artchart.database.UserAuthenticationViewModelFactory
import com.lab.artchart.database.UserViewModel
import com.lab.artchart.databinding.ActivityMainBinding
import com.lab.artchart.service.LocationService
import com.lab.artchart.service.LocationViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    // navbar
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var navController: NavController

    // database
    lateinit var artworkViewModel: ArtworkViewModel
    lateinit var userViewModel: UserViewModel
    lateinit var userAuthenticationViewModelFactory: UserAuthenticationViewModelFactory
    private lateinit var userAuthenticationViewModel: UserAuthenticationViewModel
    lateinit var reviewViewModel: ReviewViewModel

    // location
    lateinit var locationViewModel: LocationViewModel
    private val locationPermissionRequestCode = 101
    private var isServiceBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // set up global view models
        initializeViewModels()

        // set up location service
        startLocationServiceOrRequestPermission()

        // navbar
        drawerLayout = binding.drawerLayout
        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)

        setSupportActionBar(binding.appBarMain.toolbar)
        // Passing each menu ID as a set of Ids because each menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_search, R.id.nav_addArt, R.id.nav_profile, R.id.nav_signIn
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // initialize custom navigation
        handleNavigation(userAuthenticationViewModel.currentUser.value)
        // change navigation when user account created
        userAuthenticationViewModel.currentUser.observe(this) { user ->
            handleNavigation(user)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            applicationContext.unbindService(locationViewModel)
            val locationIntent = Intent(this, LocationService::class.java)
            applicationContext.stopService(locationIntent)
            isServiceBound = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // handle navigating to ProfileFragment from activities
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getStringExtra("NAVIGATE_TO") == "SignInFragment") {
            navController.navigate(R.id.nav_signIn)
        }
    }

    private fun initializeViewModels() {
        artworkViewModel = ViewModelProvider(this)[ArtworkViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        reviewViewModel = ViewModelProvider(this)[ReviewViewModel::class.java]
        userAuthenticationViewModelFactory = UserAuthenticationViewModelFactory(userViewModel, reviewViewModel)
        userAuthenticationViewModel = ViewModelProvider(this, userAuthenticationViewModelFactory)[UserAuthenticationViewModel::class.java]
        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]
    }

    // set up custom navigation depending on currently signed in user
    private fun handleNavigation(currentUser: FirebaseUser?) {
        navView.setNavigationItemSelectedListener { menuItem ->
            // show correct fragment depending on if user is already signed in
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    if (currentUser != null) {
                        navController.navigate(R.id.nav_profile)
                    } else {
                        navController.navigate(R.id.nav_signIn)
                    }

                    // close nav bar view
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> {
                    // otherwise show chosen fragment
                    navController.navigate(menuItem.itemId)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
            }
        }
    }

    // start location service if permission granted
    private fun startLocationServiceOrRequestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startAndBindLocationService()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionRequestCode)
        }
    }

    // handle denied location permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && requestCode == locationPermissionRequestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startAndBindLocationService()
            } else {
                Toast.makeText(this, "Location services needed to find art near you", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startAndBindLocationService() {
        if (!isServiceBound) {
            val locationIntent = Intent(this, LocationService::class.java)
            applicationContext.startService(locationIntent)
            applicationContext.bindService(locationIntent, locationViewModel, Context.BIND_AUTO_CREATE)
            isServiceBound = true
        }
    }
}