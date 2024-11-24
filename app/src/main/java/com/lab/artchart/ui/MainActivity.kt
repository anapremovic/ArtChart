package com.lab.artchart.ui

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
import com.lab.artchart.database.FirebaseRepository
import com.lab.artchart.database.FirebaseViewModel
import com.lab.artchart.database.FirebaseViewModelFactory
import com.lab.artchart.database.UserAuthenticationViewModel
import com.lab.artchart.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var navController: NavController

    private lateinit var binding: ActivityMainBinding

    private lateinit var repository: FirebaseRepository
    lateinit var firebaseViewModel: FirebaseViewModel
    lateinit var userAuthenticationViewModel: UserAuthenticationViewModel

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
                R.id.nav_home, R.id.nav_search, R.id.nav_addArt, R.id.nav_profile
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
        handleSignUpActivityFinished()
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

    private fun initializeViewModels() {
        repository = FirebaseRepository() // initialize repository in MainActivity so it's global
        val viewModelFactory = FirebaseViewModelFactory(repository)
        firebaseViewModel = ViewModelProvider(this, viewModelFactory)[FirebaseViewModel::class.java]
        userAuthenticationViewModel = ViewModelProvider(this)[UserAuthenticationViewModel::class.java]
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

    // handle navigating to ProfileFragment from SignUpActivity
    private fun handleSignUpActivityFinished() {
        if (intent.getStringExtra("NAVIGATE_TO") == "ProfileFragment") {
            navController.navigate(R.id.nav_profile)
        }
    }
}