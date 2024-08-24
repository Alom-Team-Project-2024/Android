package com.example.alom_team_project.notification

data class NotificationData(
    var content: String,
    var time: String,
    var isRead: Boolean = false
)