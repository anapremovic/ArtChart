package com.lab.artchart.ui.addArt

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddArtViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Add Art Fragment"
    }
    val text: LiveData<String> = _text
}