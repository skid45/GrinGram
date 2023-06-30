package com.skid.gringram.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.skid.gringram.R
import com.skid.gringram.databinding.FullScreenPhotoBinding
import com.squareup.picasso.Picasso

class FullScreenPhotoFragment(
    private val canAttach: Boolean,
    private val actionListener: FullscreenPhotoActionListener,
) : DialogFragment() {

    private val photoUri by lazy { arguments?.getString("photoUri") }
    private val position by lazy { arguments?.getInt("position") }

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
            val uri = Uri.parse(photoUri)
            if (canAttach) {
                fullscreenPhotoAttachFileMessageEditText.visibility = View.VISIBLE
                fullscreenPhotoSelectImageTextView.visibility = View.VISIBLE
                if (position != null) {
                    val indexOfUri =
                        actionListener.getListOfSelected().keys.indexOfFirst { it == position } + 1
                    if (indexOfUri > 0) {
                        fullscreenPhotoSelectImageTextView.apply {
                            background = AppCompatResources.getDrawable(
                                requireContext(), R.drawable.circle_border_filled_background
                            )
                            text = indexOfUri.toString()
                        }
                    } else {
                        actionListener.removeSelectedItem(position!!)
                        fullscreenPhotoSelectImageTextView.apply {
                            text = ""
                            background = AppCompatResources.getDrawable(
                                requireContext(), R.drawable.circle_border_background
                            )
                        }
                    }
                }
            } else {
                fullscreenPhotoAttachFileMessageEditText.visibility = View.GONE
                fullscreenPhotoSelectImageTextView.visibility = View.GONE
            }

            fullscreenPhotoSendPhotoButton.setOnClickListener {
                if (canAttach) {
                    userViewModel.companionUserForChatState.value?.uid?.let {

                        val media = actionListener.getListOfSelected().values.toMutableList()
                        if (!media.contains(uri)) media.add(uri)

                        userViewModel.sendMessage(
                            text = fullscreenPhotoAttachFileMessageEditText.text.toString().trim(),
                            media = media,
                            recipientUserUid = userViewModel.companionUserForChatState.value?.uid
                                ?: "",
                            context = requireContext()
                        )
                        fullscreenPhotoAttachFileMessageEditText.text.clear()
                    }
                } else userViewModel.changeUserPhoto(Uri.parse(photoUri))
                actionListener.onSendPhoto()
                dismiss()
            }

            fullscreenPhotoCancelButton.setOnClickListener {
                dismiss()
            }

            fullscreenPhotoSelectImageTextView.setOnClickListener {
                if (position != null) {
                    if (actionListener.getListOfSelected()[position!!] == null) {
                        actionListener.addSelectedItem(uri, position!!)
                        it.background = AppCompatResources.getDrawable(
                            requireContext(), R.drawable.circle_border_filled_background
                        )
                        fullscreenPhotoSelectImageTextView.text =
                            (actionListener.getListOfSelected().size).toString()
                    } else {
                        actionListener.removeSelectedItem(position!!)
                        fullscreenPhotoSelectImageTextView.text = ""
                        it.background = AppCompatResources.getDrawable(
                            requireContext(), R.drawable.circle_border_background
                        )
                    }
                }
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

interface FullscreenPhotoActionListener {
    fun onSendPhoto()
    fun addSelectedItem(uri: Uri, position: Int)
    fun removeSelectedItem(position: Int)
    fun getListOfSelected(): Map<Int, Uri>
}