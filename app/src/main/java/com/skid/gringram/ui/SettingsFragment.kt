package com.skid.gringram.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.skid.gringram.databinding.SettingsBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: SettingsBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels {
        AuthViewModelFactory(activity?.application as GringramApp)
    }
    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(activity?.application as GringramApp)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = SettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.currentUserState.collect {
                    if (it != null) {
                        binding.settingsCollapsingTollbar.title = it.username
                        if (it.photoUri != null) {
                            Picasso.get().load(it.photoUri).fit().centerCrop()
                                .into(binding.userImage)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                userViewModel.changeUserPhoto(uri)
            } else Toast.makeText(context, "Failed to upload image to storage", Toast.LENGTH_SHORT)
                .show()
        }

    private fun setListeners() {
        binding.apply {
            signOut.setOnClickListener {
                authViewModel.signOut()
//                navController.navigate(R.id.action_mainAppFragment_to_signInFragment)
            }
            changePhoto.setOnClickListener {
                getContent.launch("image/*")
            }
        }
    }
}