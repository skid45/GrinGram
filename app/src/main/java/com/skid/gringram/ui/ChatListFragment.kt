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
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.skid.gringram.R
import com.skid.gringram.databinding.ChatListBinding
import com.skid.gringram.ui.adapter.ChatListActionListener
import com.skid.gringram.ui.adapter.ChatListAdapter
import com.skid.gringram.ui.model.User
import com.skid.gringram.utils.Constants.COMPANION_USER
import com.skid.gringram.utils.Constants.NOTIFICATION
import com.skid.gringram.utils.customGetSerializable
import kotlinx.coroutines.launch

class ChatListFragment : Fragment(), OnScreenTouchListener {
    private var _binding: ChatListBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(activity?.application as GringramApp)
    }

    private val chatListAdapter by lazy {
        ChatListAdapter(object : ChatListActionListener {
            override fun onChatWithSelectedUser(companionUser: User) {
                val bundle = bundleOf(COMPANION_USER to companionUser)
                findNavController().navigate(
                    R.id.action_chatListFragment_to_chatFragment, bundle
                )
            }

            override fun deleteDialog(companionUserUid: String, deleteBoth: Boolean) {
                userViewModel.deleteDialog(companionUserUid, deleteBoth)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(requireContext())
                .inflateTransition(R.transition.shared_element_transition)
        sharedElementReturnTransition =
            TransitionInflater.from(requireContext())
                .inflateTransition(R.transition.shared_element_transition)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = ChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments?.getBoolean(NOTIFICATION, false) == true) {
            val bundle =
                bundleOf(COMPANION_USER to arguments?.customGetSerializable<User>(COMPANION_USER))
            arguments?.clear()
            findNavController().navigate(R.id.action_chatListFragment_to_chatFragment, bundle)
        }

        recyclerViewInit()
        setListeners()
        userViewModel.getChatListItem()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.currentUserState.collect {
                    if (it != null) {
                        userViewModel.chatListItems.collect { chatListItems ->
                            chatListAdapter.dataset = chatListItems
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

    private fun recyclerViewInit() = with(binding) {
        chatListRecyclerView.layoutManager = LinearLayoutManager(context).apply {
            reverseLayout = true
        }
        chatListRecyclerView.adapter = chatListAdapter
    }

    private fun setListeners() {
        binding.apply {
            chatListToolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.new_message_item -> {
                        val newMessageBottomSheetDialogFragment =
                            NewMessageBottomSheetDialogFragment()
                        newMessageBottomSheetDialogFragment.show(
                            requireActivity().supportFragmentManager,
                            "NewMessageBottomSheetDialogFragment"
                        )
                        true
                    }
                    else -> false
                }
            }

            searchChatList.setOnClickListener {
                val extras = FragmentNavigatorExtras(
                    chatListAppBarLayout to chatListAppBarLayout.transitionName,
                    it to it.transitionName
                )
                findNavController().navigate(
                    R.id.action_chatListFragment_to_searchChatsFragment,
                    null,
                    null,
                    extras
                )
            }
        }
    }

    override fun onScreenTouch() {
        if (chatListAdapter.listPopupWindowIsShowing) {
            chatListAdapter.listPopupWindow?.dismiss()
            chatListAdapter.listPopupWindowIsShowing = false
        }
    }
}


interface OnScreenTouchListener {
    fun onScreenTouch()
}