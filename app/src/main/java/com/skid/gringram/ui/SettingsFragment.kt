package com.skid.gringram.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.skid.gringram.R
import com.skid.gringram.databinding.SettingsBinding
import com.skid.gringram.ui.model.User
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

    private var currentUser: User = User()

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
                        currentUser = it
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

    private fun setListeners() {
        binding.apply {
            signOut.setOnClickListener {
                authViewModel.signOut()
            }

            changePhoto.setOnClickListener {
                val galleryBottomSheetDialogFragment = GalleryBottomSheetDialogFragment()
                galleryBottomSheetDialogFragment.show(
                    requireActivity().supportFragmentManager,
                    "GalleryBottomSheetDialogFragment"
                )
            }

            settingsEditButton.setOnClickListener {
                val bundle = bundleOf("currentUser" to currentUser)
                findNavController().navigate(
                    resId = R.id.action_settingsFragment_to_editProfileFragment,
                    args = bundle
                )
            }
        }
    }
}