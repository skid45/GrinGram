package com.skid.gringram.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuthException
import com.skid.gringram.R
import com.skid.gringram.databinding.SignInBinding
import kotlinx.coroutines.launch

class SignInFragment : Fragment() {
    private var _binding: SignInBinding? = null
    private val binding get() = _binding!!
    private val navController by lazy { findNavController() }
    private val authViewModel: AuthViewModel by activityViewModels()

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
                lifecycleScope.launch {
                    try {
                        authViewModel.signIn(email, password)
                    } catch (_: FirebaseAuthException) {
                        textIncorrectInput.visibility = View.VISIBLE
                    }
                    if (authViewModel.signInState.value) {
                        navController.navigate(R.id.action_signInFragment_to_mainAppFragment)
                    }
                }

            }

            textSignInToSignUp.setOnClickListener {
                navController.navigate(R.id.action_signInFragment_to_signUpFragment)
            }
        }
    }

}