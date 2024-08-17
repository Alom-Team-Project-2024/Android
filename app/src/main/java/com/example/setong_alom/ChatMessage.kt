package com.example.setong_alom

import java.util.Date

data class User(
    val nickname: String,
    val profileImage: String
)

data class UserChatRoom(
    val id: Long,
    val userId: Long,
    val roomId: Long
)

data class ChatRoom(
    val id: Long,
    val name: String,
    val createdAt: Date
)

data class ChatMessage(
    val id: Long, // 메시지 고유 번호
    val message: String, // 내용
    val createdAt: Long, // 전송 시각
)

data class ChatRoomResponse(
    val chatRoomName: String
)

data class ChatHistoryResponse(
    val messages: List<ChatMessage>
)