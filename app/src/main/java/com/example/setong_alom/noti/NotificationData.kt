package com.example.setong_alom.noti

data class NotificationData(
    var content: String,
    var time: String,
    var isRead: Boolean = false
)