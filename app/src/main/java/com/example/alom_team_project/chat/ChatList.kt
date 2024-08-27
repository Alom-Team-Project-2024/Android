package com.example.alom_team_project.chat

data class ChatList(
    val chatRoomId: Long,
    var profile: String,
    var name: String,
    var content: String,
    var time: String
)