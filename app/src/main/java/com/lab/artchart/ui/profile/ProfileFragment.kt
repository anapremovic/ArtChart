package com.lab.artchart.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseUser
import com.lab.artchart.database.UserAuthenticationViewModel
import com.lab.artchart.databinding.FragmentProfileBinding
import com.lab.artchart.ui.MainActivity
import com.lab.artchart.ui.userAuthentication.SignInFragment

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private lateinit var userAuthenticationViewModel: UserAuthenticationViewModel

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        userAuthenticationViewModel = (activity as MainActivity).userAuthenticationViewModel

        val user = userAuthenticationViewModel.currentUser.value
        if (user != null) {
            setUserProfileInformation(user)
        } else {
            startActivity(Intent(requireActivity(), SignInFragment::class.java))
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