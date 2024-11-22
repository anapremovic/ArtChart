package com.lab.artchart.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lab.artchart.databinding.FragmentProfileBinding
import com.lab.artchart.ui.userAuthentication.SignInActivity
import com.lab.artchart.ui.userAuthentication.SignUpActivity

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // set up buttons that take you to sign up and sign in pages
        binding.signUp.setOnClickListener {
            startActivity(Intent(activity, SignUpActivity::class.java))
        }
        binding.signIn.setOnClickListener {
            startActivity(Intent(activity, SignInActivity::class.java))
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}