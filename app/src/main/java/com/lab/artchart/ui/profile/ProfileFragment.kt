package com.lab.artchart.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseUser
import com.lab.artchart.R
import com.lab.artchart.database.UserAuthenticationViewModel
import com.lab.artchart.databinding.FragmentProfileBinding
import com.lab.artchart.ui.MainActivity

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private lateinit var userAuthenticationViewModel: UserAuthenticationViewModel

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        userAuthenticationViewModel = (activity as MainActivity).userAuthenticationViewModel

        userAuthenticationViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                setUserProfileInformation(user)
            } else {
                Log.w("PROFILE_FRAG", "No currently authenticated user")
            }
        }
        binding.signOutButton.setOnClickListener {
            userAuthenticationViewModel.signOut()
            Toast.makeText(requireContext(), "Signed out", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.nav_signIn)
        }

        return root
    }

    private fun setUserProfileInformation(user: FirebaseUser) {
        // TODO: set username text
        binding.emailText.text = user.email.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}