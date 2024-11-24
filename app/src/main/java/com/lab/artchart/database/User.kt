package com.lab.artchart.database

data class User(
    val authUid: String,
    val username: String,
    var profilePictureUrl: String? = null
)
