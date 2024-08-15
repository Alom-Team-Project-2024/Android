package com.example.setong_alom

import android.provider.ContactsContract.CommonDataKinds.Nickname

import com.google.gson.annotations.SerializedName
import java.util.*

data class QuestionClass(
    @SerializedName("createdAt") val createdAt: Date,
    @SerializedName("id") val id: Long,
    @SerializedName("user") val user: User,
    @SerializedName("subject") val subject: String,
    @SerializedName("text") val text: String,
    @SerializedName("writer") val writer: String,
    @SerializedName("likes") val likes: Int,
    @SerializedName("clips") val clips: Int,
    @SerializedName("replyCount") val replyCount: Int,
    @SerializedName("replies") val replies: List<Reply>
)

data class User(
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImage") val profileImage: String
)

data class Reply(
    @SerializedName("createdAt") val createdAt: Date,
    @SerializedName("id") val id: Int,
    @SerializedName("questionPost") val questionPost: String,
    @SerializedName("title") val title: String,
    @SerializedName("text") val text: String,
    @SerializedName("writer") val writer: String,
    @SerializedName("likes") val likes: Int
)

data class PostResponse(
    val id: Long
)