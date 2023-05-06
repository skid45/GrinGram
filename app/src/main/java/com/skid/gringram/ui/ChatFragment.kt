package com.skid.gringram.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.skid.gringram.databinding.ChatBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {
    private var _binding: ChatBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(activity?.application as GringramApp)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = ChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chatToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.userForChat.collect {
                    binding.apply {
                        chatToolbar.title = it.username
                        Picasso.get().load(it.photoUri).fit().centerCrop().into(chatUserImage)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}