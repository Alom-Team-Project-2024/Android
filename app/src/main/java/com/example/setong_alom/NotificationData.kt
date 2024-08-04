package com.example.setong_alom

data class NotificationData(
    var content: String,
    var time: String,
    var isRead: Boolean = false
)