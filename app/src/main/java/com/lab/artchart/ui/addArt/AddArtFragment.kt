package com.lab.artchart.ui.addArt

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lab.artchart.MainActivity
import com.lab.artchart.database.Artwork
import com.lab.artchart.database.FirebaseViewModel
import com.lab.artchart.databinding.FragmentAddArtBinding

class AddArtFragment : Fragment() {

    private var _binding: FragmentAddArtBinding? = null

    private lateinit var firebaseViewModel: FirebaseViewModel
    private var artworkImageUri: Uri? = null
    private lateinit var artworkImageView: ImageView

    // launcher for gallery permission
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            getImageFromGallery()
        } else {
            Toast.makeText(requireContext(), "Gallery permission needed to upload artwork image", Toast.LENGTH_SHORT).show()
        }
    }

    // launcher to handle selected image from gallery
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            artworkImageUri = result.data?.data
            artworkImageUri?.let {
                // update view
                artworkImageView.setImageURI(it)
            }
        }
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val addArtViewModel = ViewModelProvider(this)[AddArtViewModel::class.java]

        _binding = FragmentAddArtBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // views
        val saveButton = binding.saveButton
        val selectPhotoButton = binding.selectPhotoButton
        artworkImageView = binding.artworkImage

        // request correct permission based on android version or open gallery if already granted
        selectPhotoButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                openGalleryOrRequestPermission(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                openGalleryOrRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        // save artwork to database
        saveButton.setOnClickListener {
            firebaseViewModel = (activity as MainActivity).firebaseViewModel
            if (artworkImageUri != null) {
                // TODO: get LngLat by selecting on map
                val testArtwork = Artwork(binding.title.text.toString(),
                    binding.artistName.text.toString(),
                    binding.year.text.toString().toIntOrNull(),
                    binding.latitude.text.toString().toDoubleOrNull(),
                    binding.longitude.text.toString().toDoubleOrNull(),
                    binding.description.text.toString())

                // save to firebase realtime database and firebase storage
                firebaseViewModel.saveArtwork(testArtwork, artworkImageUri!!)
                Toast.makeText(requireContext(), "Saved Test Artwork to database", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please upload artwork image to submit", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // open gallery if permission already granted or launch permission launcher
    private fun openGalleryOrRequestPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            getImageFromGallery()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    // launch intent to open gallery
    private fun getImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }
}