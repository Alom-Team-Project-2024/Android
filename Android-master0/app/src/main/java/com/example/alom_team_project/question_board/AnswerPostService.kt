package com.example.alom_team_project.question_board

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.MultipartBody
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
    @POST("question_post/{post_id}/reply")
    fun postAnswers(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long,//질문글 id
        @Body requestBody: JsonObject
    ): Call<Long>//답변글 id!!!!!

    // 답변 불러오기
    @GET("question_post/{postId}/reply")
    fun getAnswers(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long,//질문글 id
    ): Call<List<Reply>>

//    // 이미지 업로드 및 URL 반환
//    @Multipart
//    @POST("question_post/{postId}/images")
//    fun uploadImage(
//        @Header("Authorization") token: String,
//        @Path("postId") postId: Long,
//        @Part file: MultipartBody.Part
//    ): Call<JsonArray> // 응답을 JSON으로 변경
//

}