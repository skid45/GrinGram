package com.skid.gringram.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.skid.gringram.R
import com.skid.gringram.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(application as GringramApp)
    }
    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.main_navigation_host) as NavHostFragment)
            .navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        authViewModel.signOut()
        authViewModel.checkUserSignIn(navController, R.id.signInFragment)

    }

}