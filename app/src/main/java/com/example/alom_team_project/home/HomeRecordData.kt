package com.example.alom_team_project.home

data class HomeRecordData(
    val title: String,
    val commentCount: Int,
    val imageUrl: String,
    val mentorName: String?,  // Nullable 타입으로 변경
    val answer: String
)
