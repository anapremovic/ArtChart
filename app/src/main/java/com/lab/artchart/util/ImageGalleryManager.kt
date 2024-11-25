package com.lab.artchart.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

// encapsulate image gallery logic
class ImageGalleryManager(private var imageView: ImageView) {
    var imageUri: Uri? = null

    // call inside requestPermissionLaunch
    fun handlePermission(isGranted: Boolean, context: Context, selectImageLauncher: ActivityResultLauncher<Intent>) {
        if (isGranted) {
            getImageFromGallery(selectImageLauncher)
        } else {
            Toast.makeText(context, "Gallery permission needed to upload image", Toast.LENGTH_SHORT).show()
        }
    }

    // call inside selectImageLauncher
    fun handleSelectedImage(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            imageUri?.let {
                // update view
                imageView.setImageURI(it)
            }
        }
    }

    // check which permission to request
    fun openGalleryOrRequestPermission(context: Context, requestPermissionLauncher: ActivityResultLauncher<String>, selectImageLauncher: ActivityResultLauncher<Intent>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openGalleryOrRequestPermissionInternal(Manifest.permission.READ_MEDIA_IMAGES, context, requestPermissionLauncher, selectImageLauncher)
        } else {
            openGalleryOrRequestPermissionInternal(Manifest.permission.READ_EXTERNAL_STORAGE, context, requestPermissionLauncher, selectImageLauncher)
        }
    }

    // launch select image launcher if permission already granted or launch permission launcher
    private fun openGalleryOrRequestPermissionInternal(permission: String, context: Context, requestPermissionLauncher: ActivityResultLauncher<String>, selectImageLauncher: ActivityResultLauncher<Intent>) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            // open gallery
            getImageFromGallery(selectImageLauncher)
        } else {
            // request permission
            requestPermissionLauncher.launch(permission)
        }
    }

    // launch intent to open gallery
    private fun getImageFromGallery(selectImageLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }
}