package com.lab.artchart.ui.profile

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.lab.artchart.R
import com.lab.artchart.database.UserAuthenticationViewModel
import com.lab.artchart.databinding.FragmentProfileBinding
import com.lab.artchart.ui.MainActivity
import com.lab.artchart.util.UserAuthenticationUtils

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private lateinit var userAuthenticationViewModel: UserAuthenticationViewModel

    private val binding get() = _binding!!

    private var changeEmailDialog: AlertDialog? = null
    private var newEmail: String? = null
    private var changePasswordDialog: AlertDialog? = null
    private var newPassword: String? = null
    private var confirmCredentialsDialog: AlertDialog? = null

    override fun onStart() {
        super.onStart()
        userAuthenticationViewModel.deleteSuccessful.value = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        userAuthenticationViewModel = ViewModelProvider(this, (activity as MainActivity).userAuthenticationViewModelFactory)[UserAuthenticationViewModel::class.java]

        // update UI
        setUserProfileInformation()
        userAuthenticationViewModel.toastError.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }

        // button listeners
        binding.changeEmailButton.setOnClickListener {
            openChangeEmailDialog()
        }
        binding.changePasswordButton.setOnClickListener {
            openChangePasswordDialog()
        }
        binding.signOutButton.setOnClickListener {
            openConfirmationDialog("Confirm Sign Out", "Are you sure you want to sign out?", ::signOut)
        }
        binding.deleteAccountButton.setOnClickListener {
            openConfirmationDialog("Confirm Delete Account", "Are you sure you want to delete your account? This cannot be undone.", ::deleteAccount)
        }

        // listen for successful or unsuccessful API calls
        userAuthenticationViewModel.changeEmailSent.observe(viewLifecycleOwner) { sent ->
            if (sent) {
                dismissChangeEmailDialog()
                userAuthenticationViewModel.signOut()
                Toast.makeText(requireContext(), "Please check your new email to verify", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.nav_signIn)
            }
        }
        userAuthenticationViewModel.passwordChanged.observe(viewLifecycleOwner) { changed ->
            if (changed) {
                dismissChangePasswordDialog()
                Toast.makeText(requireContext(), "Password updated", Toast.LENGTH_LONG).show()
            }
        }
        userAuthenticationViewModel.deleteSuccessful.observe(viewLifecycleOwner) { success ->
            if (success) {
                dismissReAuthenticationDialog()
                Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.nav_signIn)
            }
        }
        userAuthenticationViewModel.needReAuthenticateToChangeEmail.observe(viewLifecycleOwner) {
            openConfirmCredentialsDialog { userAuthenticationViewModel.changeEmail(newEmail!!) }
        }
        userAuthenticationViewModel.needReAuthenticateToChangePassword.observe(viewLifecycleOwner) {
            openConfirmCredentialsDialog { userAuthenticationViewModel.changePassword(newPassword!!) }
        }
        userAuthenticationViewModel.needReAuthenticateToDeleteAccount.observe(viewLifecycleOwner) {
            openConfirmCredentialsDialog { userAuthenticationViewModel.deleteAccount() }
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

    // prompt user to enter a new email
    private fun openChangeEmailDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_change_email, null)
        val emailInput = view.findViewById<EditText>(R.id.email)
        val confirmButton = view.findViewById<Button>(R.id.confirm_button)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(view)
        changeEmailDialog = builder.create()

        confirmButton.setOnClickListener {
            val email = emailInput.text.toString()
            if (UserAuthenticationUtils.verifyEmailFormat(email, emailInput)) {
                newEmail = email
                userAuthenticationViewModel.changeEmail(email)
            }
        }

        changeEmailDialog?.show()
    }

    // prompt user to enter a new password
    private fun openChangePasswordDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val passwordInput = view.findViewById<EditText>(R.id.password)
        val passwordVerifyInput = view.findViewById<EditText>(R.id.verify_password)
        val showPasswordCheckBox = view.findViewById<CheckBox>(R.id.show_password_checkbox)
        val confirmButton = view.findViewById<Button>(R.id.confirm_button)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(view)
        changePasswordDialog = builder.create()
        UserAuthenticationUtils.handleShowPasswordCheckBox(listOf(passwordInput, passwordVerifyInput), showPasswordCheckBox)

        confirmButton.setOnClickListener {
            val password = passwordInput.text.toString()
            val passwordVerify = passwordVerifyInput.text.toString()
            if (UserAuthenticationUtils.verifyPasswordNotBlank(password, passwordInput) &&
                UserAuthenticationUtils.verifyPasswordRequirements(password, passwordVerify, passwordInput, passwordVerifyInput)) {
                newPassword = password
                userAuthenticationViewModel.changePassword(password)
            }
        }

        changePasswordDialog?.show()
    }

    // generic confirmation dialog - prompt user to confirm action
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

    // prompt user to confirm their credentials
    private fun openConfirmCredentialsDialog(action: () -> Unit) {
        val view = layoutInflater.inflate(R.layout.dialog_confirm_credentials, null)
        val emailInput = view.findViewById<EditText>(R.id.email)
        val passwordInput = view.findViewById<EditText>(R.id.password)
        val confirmButton = view.findViewById<Button>(R.id.confirm_button)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(view)
        confirmCredentialsDialog = builder.create()

        confirmButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (UserAuthenticationUtils.verifyEmailFormat(email, emailInput) &&
                UserAuthenticationUtils.verifyPasswordNotBlank(password, passwordInput)) {
                userAuthenticationViewModel.reAuthenticate(email, password) {
                    // perform corresponding action after confirming credentials
                    action()
                }
            }
        }

        confirmCredentialsDialog?.show()
    }

    private fun dismissChangeEmailDialog() {
        changeEmailDialog?.dismiss()
        changeEmailDialog = null
    }
    private fun dismissChangePasswordDialog() {
        changePasswordDialog?.dismiss()
        changePasswordDialog = null
    }
    private fun dismissReAuthenticationDialog() {
        confirmCredentialsDialog?.dismiss()
        confirmCredentialsDialog = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}