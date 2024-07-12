package com.example.login.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Long = 0,
    @SerializedName("username")
    val username: String,
    val password: String
)
