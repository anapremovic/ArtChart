package com.lab.artchart.database

import com.google.firebase.database.Exclude

data class Artwork(
    val title: String? = null,
    val artistName: String? = null,
    val creationYear: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String? = null,
    var imageUrl: String? = null,
    var detectArt: Boolean? = null,
    @get:Exclude var artId: String? = null // don't store in firebase as it automatically stores the ID
)
