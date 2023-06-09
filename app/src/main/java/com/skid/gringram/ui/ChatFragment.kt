package com.skid.gringram.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skid.gringram.R
import com.skid.gringram.databinding.ChatBinding
import com.skid.gringram.ui.adapter.ChatActionListener
import com.skid.gringram.ui.adapter.ChatAdapter
import com.skid.gringram.ui.model.User
import com.skid.gringram.utils.Constants.MESSAGE_KEY_FOR_SCROLL
import com.skid.gringram.utils.customGetSerializable
import com.skid.gringram.utils.getTimeElapsedFromEpochMilliseconds
import com.skid.gringram.utils.userStateCollect
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {
    private var _binding: ChatBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(activity?.application as GringramApp)
    }

    private val chatAdapter by lazy {
        ChatAdapter(companionUser, object : ChatActionListener {
            override fun deleteMessage(messageKey: String, deleteBoth: Boolean) {
                userViewModel.deleteMessage(messageKey, deleteBoth, companionUser?.uid.toString())
            }

            override fun onFullscreenChatMedia(media: List<String>, position: Int) {
                val fullscreenChatMediaFragment = FullscreenChatMediaFragment(media, position)
                fullscreenChatMediaFragment.show(
                    requireActivity().supportFragmentManager,
                    FullscreenChatMediaFragment.TAG
                )
            }
        })
    }

    private val companionUser by lazy { arguments?.customGetSerializable<User>("companionUser") }

    private val countNewMessages: MutableStateFlow<Int> = MutableStateFlow(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = ChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCompanionUserToToolbar(companionUser)
        userViewModel.addCompanionUserForChatStateValueEventListener(companionUser!!.uid!!)

        recyclerViewInit()
        setListeners()
        userStateCollect(userViewModel.currentUserState, chatAdapter)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.companionUserForChatState.collect {
                    if (it != null) {
                        setCompanionUserToToolbar(it)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.currentUserDialogs.collect { dialogs ->
                    val dialog = dialogs.find { it.companionUserUid == companionUser?.uid }
                    if (dialog != null) {
                        val messages = dialog.messages.values.toList()
                        chatAdapter.dataset = messages
                        chatAdapter.messageKeys = dialog.messages.keys.toList()

                        countNewMessages.value = messages.count {
                            it.from == companionUser?.uid && it.viewed == false
                        }
                        if (countNewMessages.value > 0) {
                            binding.newMessagesIndicatorOnChatFab.text =
                                countNewMessages.value.toString()
                        }
                    }
                    if ((binding.chatRecyclerView.layoutManager as LinearLayoutManager)
                            .findLastVisibleItemPosition() == chatAdapter.itemCount - 2
                    ) {
                        scrollChatToLastPosition()
                    }
                    val messageKeyForScroll = arguments?.getString(MESSAGE_KEY_FOR_SCROLL)
                    if (messageKeyForScroll != null) {
                        arguments?.putString(MESSAGE_KEY_FOR_SCROLL, null)
                        val position =
                            chatAdapter.messageKeys.indexOfFirst { it == messageKeyForScroll }
                        (binding.chatRecyclerView.layoutManager as LinearLayoutManager)
                            .scrollToPositionWithOffset(position, 100)

                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userViewModel.removeCompanionUserForChatStateValueEventListener(companionUser!!.uid!!)
        _binding = null
    }

    private fun setCompanionUserToToolbar(companionUser: User?) = with(binding) {
        chatToolbar.title = companionUser?.username
        if (companionUser?.online == true) {
            chatToolbar.setSubtitleTextColor(requireActivity().getColor(R.color.colorPrimary))
            chatToolbar.subtitle = requireActivity().getString(R.string.online)
        } else {
            chatToolbar.setSubtitleTextColor(requireActivity().getColor(android.R.color.darker_gray))
            chatToolbar.subtitle =
                companionUser?.onlineTimestamp.getTimeElapsedFromEpochMilliseconds()
        }
        Picasso.get().load(companionUser?.photoUri).fit().centerCrop().into(chatUserImage)
    }

    private fun recyclerViewInit() = with(binding) {
        chatRecyclerView.layoutManager =
            LinearLayoutManager(context).apply { stackFromEnd = true }
        chatRecyclerView.adapter = chatAdapter
    }

    private fun scrollChatToLastPosition() = with(binding) {
        chatRecyclerView.postDelayed(Runnable {
            if (chatAdapter.itemCount > 0) {
                chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
            }
        }, 200)
    }

    private fun setListeners() {
        binding.apply {
            chatToolbar.apply {
                setNavigationOnClickListener {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                setOnClickListener {
                    val bundle = bundleOf("companionUser" to companionUser)
                    findNavController().navigate(
                        R.id.action_chatFragment_to_chatDetailsFragment,
                        bundle
                    )
                }
            }

            chatAttachFiles.setOnClickListener {
                val galleryBottomSheetDialogFragment =
                    GalleryBottomSheetDialogFragment(canAttach = true)
                galleryBottomSheetDialogFragment.show(
                    requireActivity().supportFragmentManager,
                    GalleryBottomSheetDialogFragment.TAG
                )
            }

            sendMessageButton.setOnClickListener {
                val messageText = messageEditText.text.toString().trim()
                userViewModel.sendMessage(
                    text = messageText,
                    recipientUserUid = companionUser!!.uid!!,
                    context = requireContext()
                )
                messageEditText.text.clear()
            }

            messageEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0.isNullOrBlank()) sendMessageButton.visibility = View.GONE
                    else sendMessageButton.visibility = View.VISIBLE
                }

                override fun afterTextChanged(p0: Editable?) {}
            })

            chatRecyclerView.addOnLayoutChangeListener { view, _, _, _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom) {
                    view.postDelayed(Runnable {
                        if (chatAdapter.itemCount > 0) {
                            (view as RecyclerView).smoothScrollToPosition(chatAdapter.itemCount - 1)
                        }
                    }, 100)
                }
            }

            chatRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (!recyclerView.canScrollVertically(1)) fabScrollChatDown.hide()
                    else if (dy < 0) {
                        fabScrollChatDown.show()
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                countNewMessages.collect {
                                    if (it > 0) {
                                        newMessagesIndicatorOnChatFab.visibility =
                                            View.VISIBLE
                                    } else newMessagesIndicatorOnChatFab.visibility =
                                        View.GONE
                                }
                            }
                        }
                    }

                    val messages = chatAdapter.dataset
                    if (messages.isNotEmpty()) {
                        val layoutManager: LinearLayoutManager =
                            binding.chatRecyclerView.layoutManager as LinearLayoutManager
                        val lastVisibleItemPosition =
                            layoutManager.findLastVisibleItemPosition()
                        if (lastVisibleItemPosition >= 0 && messages[lastVisibleItemPosition].viewed == false) {
                            val firstNotViewedPosition = messages.indexOfFirst {
                                it.from == companionUser?.uid && it.viewed == false
                            }
                            if (firstNotViewedPosition >= 0) {
                                for (index in firstNotViewedPosition..lastVisibleItemPosition) {
                                    if (messages[index].from == companionUser?.uid)
                                        userViewModel.updateMessageStatus(
                                            messageKey = chatAdapter.messageKeys[index],
                                            recipientUserUid = companionUser!!.uid!!
                                        )
                                }
                            }
                        }
                    }
                }
            })

            fabScrollChatDown.setOnClickListener {
                scrollChatToLastPosition()
            }
        }
    }
}