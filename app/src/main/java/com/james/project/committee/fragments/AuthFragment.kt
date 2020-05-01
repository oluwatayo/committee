package com.james.project.committee.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.james.project.committee.R
import com.james.project.committee.databinding.FragmentAuthBinding
import com.james.project.committee.viewmodels.AuthViewModel
import timber.log.Timber
import java.lang.Exception

class AuthFragment : Fragment() {

    private val RC_SIGN_IN: Int = 1002
    private lateinit var googleSignInClient: GoogleSignInClient
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var binding: FragmentAuthBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAuthBinding.inflate(inflater, container, false)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this.viewLifecycleOwner
        val token = getString(R.string.default_web_client_id)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(token)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        viewModel.googleSignInLiveData.observe(viewLifecycleOwner, Observer {
            if (it) {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
                viewModel.endGoogleSignIn()
            }
        })
        viewModel.signInDone.observe(viewLifecycleOwner, Observer {
            if (it) {
                findNavController().navigate(AuthFragmentDirections.actionAuthFragmentToHomeFragment())
                viewModel.endAuth()
            }
        })
        viewModel.showCompleteProfileLayoutLiveData.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.completeProfileLayout.visibility = View.VISIBLE
                binding.googleSignIn.visibility = View.GONE
            }
        })
        binding.googleSignIn.setOnClickListener { viewModel.signInWithGoogle() }
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                viewModel.firebaseAuthWithGoogle(account!!)
            } catch (e: Exception) {
                Timber.e("Google sign in failed $e")
                showSnackbarMessage("Google sign in failed")
            }
        }
    }

    fun showSnackbarMessage(message: String) =
        Snackbar.make(binding.mainCoordinator, message, Snackbar.LENGTH_LONG).show()
}