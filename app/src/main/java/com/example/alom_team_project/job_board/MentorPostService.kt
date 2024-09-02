package com.example.alom_team_project.job_board

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface MentorPostService {
    // 포스트 생성
    @POST("mentor_post")
    fun postMentor(
        @Header("Authorization") token: String,
        @Body requestBody: JsonObject
    ): Call<Void>


    // Mentor 카테고리 목록 가져오기
    @GET("mentor_post/category/{category}")
    fun getMentors(
        @Header("Authorization") token: String,
        @Path("category") category: String
    ): Call<List<MentorClass>>

    // postId를 통해 특정글 불러오기
    @GET("mentor_post/{mentorId}")
    fun getMentorFromId(
        @Header("Authorization") token: String,
        @Path("mentorId") mentorId: Long
    ): Call<MentorClass>


    // 스크랩 요청
    @POST("mentor_post/scrap/{username}/{mentorId}")
    fun scrapPost(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Path("mentorId") mentorId: Long
    ): Call<Void>

    @GET("/mentor_post") //메인페이지 구인 게시판 조회
    fun getMentors(@Header("Authorization") token: String): Call<List<MentorClass>>
}