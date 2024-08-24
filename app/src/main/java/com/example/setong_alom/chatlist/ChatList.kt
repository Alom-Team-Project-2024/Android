package com.example.setong_alom.chatlist

data class ChatList(
    val chatRoomId: Long,
    var profile: Int,
    var name: String,
    var content: String,
    var time: String
)