package com.example.alom_team_project.login

data class AuthUserDTO(
    val username: String,
    val name: String,
    val major: String,
    val studentGrade: Int,
    val registrationStatus: RegistrationStatus,
    val nickname: String?
)
