package com.skid.gringram.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.skid.gringram.R
import com.skid.gringram.databinding.FragmentMainAppBinding

class MainAppFragment : Fragment() {

    private var _binding: FragmentMainAppBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentMainAppBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = activity?.findNavController(R.id.bottom_navigation_host)
        if (navController != null) {
            binding.bottomNavigationView.setupWithNavController(navController)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}