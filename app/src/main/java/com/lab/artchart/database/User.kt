package com.lab.artchart.database

import com.google.firebase.database.Exclude

data class User(
    val username: String = "",
    var profilePictureUrl: String = "",
    @get:Exclude var uid: String? = null // don't store in firebase as it automatically stores the ID
)
