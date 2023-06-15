package com.skid.gringram.ui

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            Toast.makeText(this, "Notifications are disabled", Toast.LENGTH_SHORT).show()
        }
    }

    private val firebaseAuthStateListener = FirebaseAuth.AuthStateListener {
        if (it.currentUser == null) {
            when (navController.currentDestination?.id) {
                R.id.chatListFragment -> navController.navigate(R.id.action_chatListFragment_to_signInFragment)
                R.id.settingsFragment -> navController.navigate(R.id.action_settingsFragment_to_signInFragment)
            }
        } else {
            if (navController.currentDestination?.id == R.id.signInFragment) {
                navController.navigate(
                    R.id.action_signInFragment_to_chatListFragment,
                    intent.extras
                )
            }

            askNotificationPermission()

            val sharedPref = getSharedPreferences("token", Context.MODE_PRIVATE)
            val token = sharedPref.getString("token", "")
            if (token != null) {
                userViewModel.sendTokenToServer(token)
            }

            createNotificationChannel()

            userViewModel.changeUserOnlineStatus(isOnline = true)

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

    override fun onPause() {
        super.onPause()
        if (auth.uid != null) {
            userViewModel.changeUserOnlineStatus(isOnline = false)
        }
    }

    override fun onResume() {
        super.onResume()
        if (auth.uid != null) {
            userViewModel.changeUserOnlineStatus(isOnline = true)
        }
        cancelNotifications()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                AlertDialog.Builder(this)
                    .setTitle("Permission to send notifications")
                    .setMessage("This app can send you notifications about new messages and events. Do you want to enable notifications?")
                    .setPositiveButton("OK") { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    .setNegativeButton("No, thanks", null)
                    .show()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun cancelNotifications() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.notification_channel_id)
            val channelName = "Message Notification Channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1
    }
}