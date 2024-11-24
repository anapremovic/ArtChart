package com.lab.artchart.ui.userAuthentication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lab.artchart.R
import com.lab.artchart.database.UserAuthenticationViewModel
import com.lab.artchart.databinding.FragmentSignInBinding
import com.lab.artchart.ui.MainActivity
import com.lab.artchart.util.UserAuthenticationUtils

class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private lateinit var userAuthenticationViewModel: UserAuthenticationViewModel

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        val root: View = binding.root
        userAuthenticationViewModel = (activity as MainActivity).userAuthenticationViewModel

        // sign into existing account with user input or show error
        binding.signInButton.setOnClickListener {
            userAuthenticationViewModel.signIn(binding.email.text.toString(), binding.password.text.toString())
        }
        // on successful sign in go back to profile
        userAuthenticationViewModel.signInSuccessful.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "Signed in to ArtChart", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.nav_profile)
        }

        // update UI
        UserAuthenticationUtils.updateErrorMessages(userAuthenticationViewModel, viewLifecycleOwner, binding.email, binding.password)
        userAuthenticationViewModel.toastError.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
        UserAuthenticationUtils.handleShowPasswordCheckBox(listOf(binding.password), binding.showPasswordCheckbox)

        // go to sign up screen
        binding.signUpLink.setOnClickListener {
            startActivity(Intent(requireActivity(), SignUpActivity::class.java))
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}