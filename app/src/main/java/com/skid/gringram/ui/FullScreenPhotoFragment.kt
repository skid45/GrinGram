package com.skid.gringram.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.skid.gringram.R
import com.skid.gringram.databinding.FullScreenPhotoBinding
import com.squareup.picasso.Picasso

class FullScreenPhotoFragment(private val onSendPhotoListener: OnSendPhotoListener) :
    DialogFragment() {

    private val photoUri by lazy { arguments?.getString("photoUri") }

    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(activity?.application as GringramApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullscreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FullScreenPhotoBinding.inflate(inflater, container, false)

        Picasso.get().load(photoUri).into(binding.fullscreenPhotoPhotoView)

        binding.apply {
            fullscreenPhotoSendPhotoButton.setOnClickListener {
                userViewModel.changeUserPhoto(Uri.parse(photoUri))
                onSendPhotoListener.onSendPhoto()
                dismiss()
            }

            fullscreenPhotoCancelButton.setOnClickListener {
                dismiss()
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }
}

interface OnSendPhotoListener {
    fun onSendPhoto()
}