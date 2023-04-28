package com.skid.gringram.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.skid.gringram.R
import com.skid.gringram.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.main_navigation_host) as NavHostFragment)
            .navController
    }

    private val bottomNavFragments =
        setOf(R.id.contactListFragment, R.id.chatListFragment, R.id.settingsFragment)

    private val auth = FirebaseAuth.getInstance()


    private val firebaseAuthStateListener = FirebaseAuth.AuthStateListener {
        if (it.currentUser == null) {
            when (navController.currentDestination?.id) {
                R.id.chatListFragment -> navController.navigate(R.id.action_chatListFragment_to_signInFragment)
                R.id.settingsFragment -> navController.navigate(R.id.action_settingsFragment_to_signInFragment)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bottomNavigationView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (bottomNavFragments.contains(destination.id)) {
                binding.bottomNavigationView.visibility = View.VISIBLE
            } else {
                binding.bottomNavigationView.visibility = View.GONE
            }
        }
        supportActionBar?.hide()
        auth.addAuthStateListener(firebaseAuthStateListener)
    }
}