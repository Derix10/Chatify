package com.example.chatapp.model

import java.io.Serializable

data class User(
    val id: String,
    val image: String,
    val name: String,
    val email: String? = null,
    val token: String? = null
) : Serializable