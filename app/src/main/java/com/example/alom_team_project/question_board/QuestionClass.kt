package com.example.alom_team_project.question_board

import android.media.Image
import android.provider.ContactsContract.CommonDataKinds.Nickname

import com.google.gson.annotations.SerializedName
import java.util.*

data class QuestionClass(
    val id : Long,
    val subject: String,
    val text: String,
    val writer: String,
    val username: String,
    val likes: Int,
    val scrapCount: Int,
    val replyCount: Int,
    val replies: List<Reply>,
    val images: List<ImageData>,
    val createdAt: String
)

data class ImageData(
    val imageUrl: String
)

data class User(
    val nickname: String,
    val profileImage: String
)

data class Reply(
    val id : Long,
    val title: String,
    val text: String,
    val writer: String,
    val username: String,
    val likes: Int,
    val images: List<ImageData>,
    val createdAt: String
)

data class Subject(
    val subject : String
)

