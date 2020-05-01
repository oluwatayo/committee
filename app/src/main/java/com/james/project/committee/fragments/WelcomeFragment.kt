package com.james.project.committee.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.james.project.committee.databinding.FragmentWelcomeScreenBinding
import com.james.project.committee.viewmodels.AuthViewModel
import com.pixplicity.easyprefs.library.Prefs


const val HAS_SIGNED_IN = "has_signed_in"

class WelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentWelcomeScreenBinding.inflate(inflater, container, false)
        val viewModel: AuthViewModel by viewModels()
        viewModel.signInDone.observe(viewLifecycleOwner, Observer {
            if (it) {
                findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToHomeFragment())
                viewModel.endAuth()
            }
        })
        binding.startButton.setOnClickListener {
            findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToAuthFragment())
            Prefs.putBoolean(HAS_SIGNED_IN, true)
        }
        if (Prefs.contains(HAS_SIGNED_IN)) {
            findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToAuthFragment())
        }
        return binding.root
    }
}