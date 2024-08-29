package com.example.alom_team_project.chat

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
    val createdAt: String
)

data class ChatMessage(
    val chatRoomId: Long,
    val sender: String,
    val message: String
)

data class ChatRoomResponse(
    val id: Long,
    val chatRoomName: String,
    val userResponseList: List<UserResponse>,
    val createdAt: String,
    val modifiedAt: String
)

data class UserResponse(
    val id: Long,
    val username: String,
    val name: String,
    val nickname: String,
    val profileImage: String,
    val major: String,
    val studentCode: Int,
    val studentGrade: Int,
    val registrationStatus: RegistrationStatus,
    val role: UserRole,
    val point: Double,
    val createdAt: String,
    val modifiedAt: String
)

data class UserRate(
    val point: Double
)

data class ChatHistoryResponse(
    val chatRoomId: String,
    val sender: String,
    val message: String,
    val createdAt: String
)

enum class RegistrationStatus {
    ATTENDING,
    TAKEOFFSCHOOL,
    GRADUATE
}

enum class UserRole {
    DELETE_ACCOUNT,
    USER,
    ADMIN
}