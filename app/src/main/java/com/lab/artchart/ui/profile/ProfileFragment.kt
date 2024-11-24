package com.lab.artchart.ui.profile

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

        // update UI
        setUserProfileInformation()

        // button listeners
        binding.signOutButton.setOnClickListener {
            openSignOutConfirmationDialog()
        }

        return root
    }

    // update UI with currently signed in user
    private fun setUserProfileInformation() {
        userAuthenticationViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                // TODO: set username text
                binding.emailText.text = user.email.toString()
            } else {
                // unexpected behaviour - fragment should only be accessible when there is a user signed in
                Log.w("PROFILE_FRAG", "No currently authenticated user")
            }
        }
    }

    // confirmation dialog
    private fun openSignOutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirm Sign Out")
        builder.setMessage("Are you sure you want to sign out?")

        // sign user out and navigate back to sign in page
        builder.setPositiveButton("Yes") { _, _ ->
            userAuthenticationViewModel.signOut()
            Toast.makeText(requireContext(), "Signed out", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.nav_signIn)
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}