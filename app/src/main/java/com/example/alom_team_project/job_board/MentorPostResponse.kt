package com.example.alom_team_project.job_board

data class MentorPostResponse(
    val category: String,
    val title: String,
    val text: String,
    val writer: String,
    val username: String,
    val major: String,
    val likes: Int,
    val createdAt: String,
    val modifiedAt: String
)

data class MentorPostDTO(
    val title: String,
    val text: String,
    val category: String
)
