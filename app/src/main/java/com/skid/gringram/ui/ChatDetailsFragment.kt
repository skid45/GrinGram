package com.skid.gringram.ui

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skid.gringram.R
import com.skid.gringram.databinding.ChatDetailsBinding
import com.skid.gringram.databinding.CustomAlertDialogLayoutBinding
import com.skid.gringram.ui.adapter.GalleryActionListener
import com.skid.gringram.ui.adapter.GalleryAdapter
import com.skid.gringram.ui.model.Dialog
import com.skid.gringram.ui.model.User
import com.skid.gringram.utils.customGetSerializable
import com.skid.gringram.utils.getTimeElapsedFromEpochMilliseconds
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ChatDetailsFragment : Fragment() {

    private var _binding: ChatDetailsBinding? = null
    private val binding get() = _binding!!

    private var media = emptyList<String>()

    private val galleryAdapter by lazy {
        GalleryAdapter(false,
            object : GalleryActionListener {
                override fun onFullScreenPhoto(uri: Uri, position: Int) {
                    val fullscreenChatMediaFragment = FullscreenChatMediaFragment(media, position)
                    fullscreenChatMediaFragment.show(
                        requireActivity().supportFragmentManager,
                        FullscreenChatMediaFragment.TAG
                    )
                }

                override fun addSelectedItem(uri: Uri, position: Int) {}

                override fun removeSelectedItem(position: Int) {}

                override fun getListOfSelected(): Map<Int, Uri> = emptyMap()

                override fun getStateOfSelected(): MutableStateFlow<LinkedHashMap<Int, Uri>> =
                    MutableStateFlow(linkedMapOf())
            })
    }

    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(activity?.application as GringramApp)
    }

    private val companionUser by lazy { arguments?.customGetSerializable<User>("companionUser") }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = ChatDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCompanionUserDetails()
        setContactInteractionButton()
        recyclerViewInit()
        setDialogListener()
        setListeners()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setCompanionUserDetails() = with(binding) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.companionUserForChatState.collect {
                    if (it != null) {
                        Picasso.get().load(it.photoUri).fit().centerCrop()
                            .into(chatDetailsUserImage)
                        chatDetailsCollapsingToolbar.title = it.username
                        setUserOnlineStatus(it.online, it.onlineTimestamp)
                    }
                }
            }
        }
    }

    private fun setDialogListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.currentUserDialogs.collect { dialogs ->
                    val dialog = dialogs.firstOrNull { it.companionUserUid == companionUser?.uid }
                    if (dialog != null) {
                        setMuteButton(dialog.mute)
                        galleryAdapter.submitList(dialog.media.map { Uri.parse(it) })
                        media = dialog.media
                    }
                }
            }
        }
    }

    private fun recyclerViewInit() {
        binding.chatDetailsMediaRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = galleryAdapter
        }

    }

    private fun setListeners() {
        binding.apply {
            chatDetailsToolbar.setNavigationOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            chatDetailsMuteUnmuteButton.setOnClickListener {
                val mute =
                    if (chatDetailsMuteUnmuteButton.text == getString(R.string.mute)) Dialog.SOUND_OFF
                    else Dialog.SOUND_ON
                userViewModel.updateDialogMute(mute, companionUser?.uid.toString())
            }

            chatDetailsAddRemoveContactButton.setOnClickListener {
                companionUser?.uid?.let {
                    if (chatDetailsAddRemoveContactButton.text == getString(R.string.add_contact)) {
                        userViewModel.addContact(it)
                    } else userViewModel.removeContact(it)
                }
            }

            chatDetailsDeleteDialogButton.setOnClickListener {
                showDeleteMessageAlertDialog(requireContext())
            }
        }
    }

    private fun setUserOnlineStatus(isOnline: Boolean, onlineTimestamp: Long?) =
        with(binding.chatDetailsUserOnline) {
            if (isOnline) {
                text = getString(R.string.online)
                setTextColor(requireContext().getColor(R.color.colorPrimary))
            } else {
                setTextColor(requireContext().getColor(android.R.color.darker_gray))
                text = onlineTimestamp.getTimeElapsedFromEpochMilliseconds()
            }
        }

    private fun setMuteButton(isMuted: Boolean) = with(binding.chatDetailsMuteUnmuteButton) {
        if (isMuted) {
            icon = AppCompatResources.getDrawable(
                requireContext(), R.drawable.baseline_notifications_off_24
            )
            text = getString(R.string.unmute)
        } else {
            icon = AppCompatResources.getDrawable(
                requireContext(), R.drawable.baseline_notifications_24
            )
            text = getString(R.string.mute)
        }
    }

    private fun setContactInteractionButton() = with(binding.chatDetailsAddRemoveContactButton) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.currentUserContactList.collect { contacts ->
                    if (contacts.firstOrNull { it.uid == companionUser?.uid } != null) {
                        icon = AppCompatResources.getDrawable(
                            requireContext(), R.drawable.baseline_person_remove_24
                        )
                        getString(R.string.remove_contact).let {
                            text = it
                            contentDescription = it
                        }
                    } else {
                        icon = AppCompatResources.getDrawable(
                            requireContext(), R.drawable.baseline_person_add_24
                        )
                        getString(R.string.add_contact).let {
                            text = it
                            contentDescription = it
                        }
                    }
                }
            }
        }
    }

    private fun showDeleteMessageAlertDialog(context: Context) {
        val customAlertDialogLayoutBinding =
            CustomAlertDialogLayoutBinding.inflate(LayoutInflater.from(context))
        val checkBoxText = "Also delete for ${companionUser?.username}"
        customAlertDialogLayoutBinding.customAlertDialogTitle.text =
            context.getString(R.string.delete_dialog)
        customAlertDialogLayoutBinding.customAlertDialogAlertMessage.text =
            context.getString(R.string.are_you_sure_you_want_to_delete_this_dialog)
        customAlertDialogLayoutBinding.customAlertDialogCheckbox.text = checkBoxText
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder
            .setView(customAlertDialogLayoutBinding.root)
            .setPositiveButton("Delete") { _, _ ->
                userViewModel.deleteDialog(
                    companionUser?.uid.toString(),
                    customAlertDialogLayoutBinding.customAlertDialogCheckbox.isChecked
                )
            }
            .setNegativeButton("Cancel") { _, _ -> }
        val dialog = alertDialogBuilder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(context.getColor(R.color.colorError))
        }
        dialog.show()
    }
}