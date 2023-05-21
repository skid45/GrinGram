package com.skid.gringram.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skid.gringram.databinding.ChatBinding
import com.skid.gringram.ui.adapter.ChatAdapter
import com.skid.gringram.ui.model.User
import com.skid.gringram.utils.customGetSerializable
import com.skid.gringram.utils.userStateCollect
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {
    private var _binding: ChatBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(activity?.application as GringramApp)
    }

    private val chatAdapter by lazy { ChatAdapter() }

    private val companionUser by lazy { arguments?.customGetSerializable<User>("companionUser") }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = ChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            chatToolbar.title = companionUser!!.username
            Picasso.get().load(companionUser!!.photoUri).fit().centerCrop().into(chatUserImage)
        }

        recyclerViewInit()
        setListeners()
        userStateCollect(userViewModel.currentUserState, chatAdapter)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.currentUserDialogs.collect { dialogs ->
                    val dialog = dialogs.find { it.companionUserUid == companionUser?.uid }
                    if (dialog != null) {
                        val messages = dialog.messages.values.toList()
                        chatAdapter.dataset = messages
                        chatAdapter.messageKeys = dialog.messages.keys.toList()
                    }
                    scrollChatToLastPosition()
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun recyclerViewInit() = with(binding) {
        chatRecyclerView.layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
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
            chatToolbar.setNavigationOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            sendMessageButton.setOnClickListener {
                userViewModel.sendMessage(
                    messageEditText.text.toString().trim(),
                    companionUser!!.uid!!
                )
                messageEditText.text.clear()
            }

            messageEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

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
                                            chatAdapter.messageKeys[index],
                                            companionUser!!.uid!!
                                        )
                                }
                            }
                        }
                    }
                }
            })
        }
    }
}