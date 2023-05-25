package com.skid.gringram.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.skid.gringram.R
import com.skid.gringram.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.main_navigation_host) as NavHostFragment)
            .navController
    }

    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(application as GringramApp)
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
        } else {
            navController.navigate(R.id.action_signInFragment_to_chatListFragment)

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    userViewModel.currentUserDialogs.collect { dialogs ->
                        val countNewMessage = dialogs.sumOf { dialog ->
                            dialog.messages.values.count { message ->
                                message.from == dialog.companionUserUid && message.viewed == false
                            }
                        }
                        if (countNewMessage > 0) {
                            binding.bottomNavigationView
                                .getOrCreateBadge(R.id.chatListFragment)
                                .number = countNewMessage
                        } else {
                            binding.bottomNavigationView.removeBadge(R.id.chatListFragment)
                        }
                    }
                }
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
        auth.addAuthStateListener(firebaseAuthStateListener)
    }
}