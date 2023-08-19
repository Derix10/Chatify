package com.example.chatapp.model

import java.util.Date

data class ChatMessage(
    val senderId: String? = null,
    val receivedId: String? = null,
    var message: String? = null,
    val dateTime: String? = null,
    var dateObject: Date? = null,
    var conversionId: String? = null,
    var conversionName: String? = null,
    var conversionImage: String? = null
)