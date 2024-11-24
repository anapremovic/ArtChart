package com.lab.artchart.database

data class User(
    val authIud: Long,
    val username: String,
    var profilePictureUrl: String? = null
)
