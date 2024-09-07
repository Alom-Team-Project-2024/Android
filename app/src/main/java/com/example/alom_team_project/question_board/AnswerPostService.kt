package com.example.alom_team_project.question_board


import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface AnswerPostService {
    // 답변 등록
    @POST("question_post/{postId}/reply")
    fun postAnswer(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long,//질문글 id
        @Body requestBody: JsonObject
    ): Call<Long>//답변글 id!!!!!

    // 답변 불러오기
    @GET("question_post/{postId}/reply")
    fun getAnswers(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long//질문글 id
    ): Call<List<Reply>>

    // postId를 통해 특정글 불러오기
    @GET("question_post/{postId}")
    fun getQuestionFromId(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long
    ): Call<QuestionClass>

    // 질문글 좋아요 요청
    @POST("question_post/{postId}/likes/up")
    fun likePost(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long
    ): Call<Void>

    // 답변글 좋아요 요청
    @POST("question_post/reply/{replyId}/likes/up")
    fun likeReply(
        @Header("Authorization") token: String,
        @Path("replyId") replyId: Long
    ): Call<Void>

    // 스크랩 요청
    @POST("question_post/scrap/{username}/{postId}")
    fun scrapPost(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Path("postId") postId: Long
    ): Call<Void>

    // 이미지 업로드 및 URL 반환
    @Multipart
    @POST("question_post/{postId}/reply/images")
    fun uploadImages(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long,
        @Part file: List<MultipartBody.Part>
    ): Call<JsonArray> // 응답을 JSON으로 변경

    //username으로 user조회
    @GET("users/username/{username}")
    fun getProfile(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Call<User>

}