package com.example.alom_team_project.model

import com.example.alom_team_project.RegistrationStatus

data class AuthUserDTO(
    val username: String,
    val name: String,
    val major: String,
    val studentGrade: Int,
    val registrationStatus: RegistrationStatus
)
