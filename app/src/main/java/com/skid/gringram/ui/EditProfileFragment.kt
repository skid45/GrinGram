package com.skid.gringram.ui

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.skid.gringram.R
import com.skid.gringram.databinding.EditProfileBinding
import com.skid.gringram.ui.model.User
import com.skid.gringram.utils.customGetSerializable
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment() {

    private var _binding: EditProfileBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(requireActivity().application as GringramApp)
    }

    private val currentUser by lazy { arguments?.customGetSerializable<User>("currentUser") }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = EditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            editProfileUsernameEditText.setText(currentUser?.username.toString())
            Picasso.get().load(currentUser?.photoUri).fit().centerCrop().into(editProfileUserImage)
        }
        setListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                binding.editProfileUserImage.setImageURI(uri)
                userViewModel.changeUserPhoto(uri)
            } else Toast.makeText(context, "Failed to upload image to storage", Toast.LENGTH_SHORT)
                .show()
        }

    private fun setListeners() {
        binding.apply {
            editProfileCancelButton.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            editProfileSetNewPhotoButton.setOnClickListener {
                getContent.launch("image/*")
            }

            editProfileDoneButton.setOnClickListener {
                if (editProfileUsernameError.visibility == View.GONE) {
                    userViewModel.changeUsername(editProfileUsernameEditText.text.toString())
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }

            editProfileUsernameEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (!p0.isNullOrBlank()) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                userViewModel.usernameIsValid.collect {
                                    if (it) {
                                        editProfileUsernameError.text =
                                            getString(R.string.a_user_with_such_a_username_already_exists)
                                        editProfileUsernameError.visibility = View.VISIBLE
                                    } else editProfileUsernameError.visibility = View.GONE
                                }
                            }
                        }
                        userViewModel.validateUsername(p0.toString())

                    } else {
                        editProfileUsernameError.text = getString(R.string.username_cannot_be_empty)
                        editProfileUsernameError.visibility = View.VISIBLE
                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                }

            })
        }
    }
}