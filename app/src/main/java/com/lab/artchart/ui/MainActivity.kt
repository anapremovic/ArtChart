package com.lab.artchart.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseUser
import com.lab.artchart.R
import com.lab.artchart.database.ArtworkViewModel
import com.lab.artchart.database.UserAuthenticationViewModel
import com.lab.artchart.database.UserAuthenticationViewModelFactory
import com.lab.artchart.database.UserViewModel
import com.lab.artchart.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // navbar
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var navController: NavController

    private lateinit var binding: ActivityMainBinding

    // view models
    lateinit var artworkViewModel: ArtworkViewModel
    lateinit var userViewModel: UserViewModel
    lateinit var userAuthenticationViewModelFactory: UserAuthenticationViewModelFactory
    private lateinit var userAuthenticationViewModel: UserAuthenticationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // set up global view models
        initializeViewModels()

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // handle navigating to ProfileFragment from SignUpActivity
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getStringExtra("NAVIGATE_TO") == "SignInFragment") {
            navController.navigate(R.id.nav_signIn)
        }
    }

    private fun initializeViewModels() {
        artworkViewModel = ViewModelProvider(this)[ArtworkViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        userAuthenticationViewModelFactory = UserAuthenticationViewModelFactory(userViewModel)
        userAuthenticationViewModel = ViewModelProvider(this, userAuthenticationViewModelFactory)[UserAuthenticationViewModel::class.java]
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
}