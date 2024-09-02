package com.example.alom_team_project.home

import com.example.alom_team_project.question_board.ImageData

data class HomeRecordData(
    val title: String,
    val commentCount: Int,
    val images: List<ImageData>,
    val mentorName: String?,  // Nullable 타입으로 변경
    val answer: String,
    val id: Long  )


data class ImageData(
    val imageUrl: String
)