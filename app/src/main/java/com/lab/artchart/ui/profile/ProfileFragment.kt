package com.lab.artchart.ui.profile

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.lab.artchart.R
import com.lab.artchart.database.UserAuthenticationViewModel
import com.lab.artchart.databinding.FragmentProfileBinding
import com.lab.artchart.util.UserAuthenticationUtils

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private lateinit var userAuthenticationViewModel: UserAuthenticationViewModel

    private val binding get() = _binding!!

    private var reAuthenticationDialog: AlertDialog? = null

    override fun onStart() {
        super.onStart()
        userAuthenticationViewModel.deleteSuccessful.value = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        userAuthenticationViewModel = ViewModelProvider(this)[UserAuthenticationViewModel::class.java]

        // update UI
        setUserProfileInformation()
        userAuthenticationViewModel.toastError.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }

        // button listeners
        binding.signOutButton.setOnClickListener {
            openConfirmationDialog("Confirm Sign Out", "Are you sure you want to sign out?", ::signOut)
        }
        binding.deleteAccountButton.setOnClickListener {
            openConfirmationDialog("Confirm Delete Account", "Are you sure you want to delete your account? This cannot be undone.", ::deleteAccount)
        }

        // listen for successful or unsuccessful API calls
        userAuthenticationViewModel.deleteSuccessful.observe(viewLifecycleOwner) { success ->
            if (success) {
                dismissReAuthenticationDialog()
                Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.nav_signIn)
            }
        }
        userAuthenticationViewModel.needReAuthenticate.observe(viewLifecycleOwner) {
            openReAuthenticationDialog()
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

    // generic confirmation dialog
    private fun openConfirmationDialog(title: String, message: String, action: () -> Unit) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton("Yes") { _, _ ->
            action()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun signOut() {
        userAuthenticationViewModel.signOut()
        Toast.makeText(requireContext(), "Signed out", Toast.LENGTH_LONG).show()
        findNavController().navigate(R.id.nav_signIn)
    }

    private fun deleteAccount() {
        userAuthenticationViewModel.deleteAccount()
    }

    private fun openReAuthenticationDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_confirm_credentials, null)
        val emailInput = view.findViewById<EditText>(R.id.email)
        val passwordInput = view.findViewById<EditText>(R.id.password)
        val confirmButton = view.findViewById<Button>(R.id.confirm_button)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(view)
        reAuthenticationDialog = builder.create()

        UserAuthenticationUtils.updateErrorMessages(userAuthenticationViewModel, viewLifecycleOwner, emailInput, passwordInput)

        confirmButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            userAuthenticationViewModel.reAuthenticate(email, password) {
                // delete account after re-authenticating
                userAuthenticationViewModel.deleteAccount()
            }
        }

        reAuthenticationDialog?.show()
    }

    private fun dismissReAuthenticationDialog() {
        reAuthenticationDialog?.dismiss()
        reAuthenticationDialog = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}