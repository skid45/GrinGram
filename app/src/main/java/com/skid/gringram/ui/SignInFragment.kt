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
import androidx.navigation.fragment.findNavController
import com.skid.gringram.R
import com.skid.gringram.databinding.SignInBinding
import kotlinx.coroutines.launch

class SignInFragment : Fragment() {
    private var _binding: SignInBinding? = null
    private val binding get() = _binding!!
    private val navController by lazy { findNavController() }
    private val authViewModel: AuthViewModel by activityViewModels {
        AuthViewModelFactory(activity?.application as GringramApp)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = SignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun setListeners() {
        binding.apply {
            buttonSignIn.setOnClickListener {
                val email = edittextEmail.text.toString()
                val password = edittextPassword.text.toString()
                if (email.isBlank() || password.isBlank()) {
                    if (email.isBlank()) edittextEmail.error = "Field cannot be empty"
                    if (password.isBlank()) edittextPassword.error = "Field cannot be empty"
                    return@setOnClickListener
                }
                authViewModel.signIn(email, password)
                lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        authViewModel.signInState.collect {
                            if (it == true) {
                                if (navController.currentDestination!!.id == R.id.signInFragment) {
                                    navController.navigate(R.id.action_signInFragment_to_chatListFragment)
                                }
                            } else if (it == false) {
                                textIncorrectInput.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }

            textSignInToSignUp.setOnClickListener {
                navController.navigate(R.id.action_signInFragment_to_signUpFragment)
            }
        }
    }
}
