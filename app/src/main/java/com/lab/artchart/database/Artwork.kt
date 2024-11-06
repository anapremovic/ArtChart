package com.lab.artchart.database

data class Artwork(
    val title: String? = null,
    val artistName: String? = null,
    val creationYear: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String? = null,
    var imageUrl: String? = null
)
