package com.example.alom_team_project

data class UserResponse(
    val username: String,
    val name: String,
    val nickname: String,
    val profileImage: String,
    val major: String,
    val studentCode: Int,
    val studentGrade: Int,
    val registrationStatus: String,
    val role: String,
    val point: Double
)