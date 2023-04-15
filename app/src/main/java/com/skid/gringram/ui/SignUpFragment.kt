package com.skid.gringram.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuthException
import com.skid.gringram.R
import com.skid.gringram.databinding.SignUpBinding
import kotlinx.coroutines.launch

class SignUpFragment : Fragment() {
    private var _binding: SignUpBinding? = null
    private val binding get() = _binding!!
    private val navController by lazy { findNavController() }
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = SignUpBinding.inflate(inflater, container, false)
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
            buttonSignUp.setOnClickListener {
                val username = edittextUsername.text.toString()
                val email = edittextEmail.text.toString()
                val password = edittextPassword.text.toString()
                if (username.isBlank() || email.isBlank() || password.isBlank()) {
                    if (username.isBlank()) edittextUsername.error = "Field cannot be empty"
                    if (email.isBlank()) edittextEmail.error = "Field cannot be empty"
                    if (password.isBlank()) edittextPassword.error = "Field cannot be empty"
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    try {
                        authViewModel.signUp(email, password)
                    } catch (e: FirebaseAuthException) {
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                    if (authViewModel.signUpState.value) {
                        navController.navigate(R.id.action_signUpFragment_to_mainAppFragment)
                    }

                }
            }

            textSignUpToSignIn.setOnClickListener {
                navController.navigate(R.id.action_signUpFragment_to_signInFragment)
            }
        }
    }
}