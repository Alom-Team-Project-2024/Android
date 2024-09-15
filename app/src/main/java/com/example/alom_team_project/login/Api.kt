package com.example.alom_team_project.login

import QuestionPostResponse
import com.example.alom_team_project.job_board.MentorPostResponse
import com.example.alom_team_project.mypage.UserResponse
import com.example.alom_team_project.job_board.MentorClass
import com.example.alom_team_project.question_board.QuestionClass
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface SejongApi {
    // 세종대학교 로그인 API
    @POST("/auth?method=ClassicSession")
    fun login(@Body body: Map<String, String>): Call<SejongAuthResponse>
}

interface UserApi {
    @POST("/users/login") // 동일한 엔드포인트로 사용자 정보 전송 및 JWT 토큰 요청
    fun requestJwtToken(@Body userDTO: AuthUserDTO): Call<JwtResponse>

    @PATCH("/users") // 프로필 닉네임 변경사항 전송
    fun updateProfile(
        @Header("Authorization") authHeader: String, // JWT 토큰을 헤더에 추가
        @Body profileData: Map<String, String> // 학번과 닉네임
    ): Call<String> // 응답을 String으로 받음

    @GET("users/{userId}/profile-image") // 프로필 이미지 조회
    fun getProfileImage(
        @Path("userId") userId: String,
        @Header("Authorization") authHeader: String
    ): Call<ResponseBody>

    @Multipart
    @POST("users/{username}/profile-image") //프로필 이미지 변경
    fun uploadProfileImage(
        @Path("username") username: String,
        @Header("Authorization") authHeader: String,
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>


    @GET("/users/username/{username}") // 사용자 정보 조회
    fun getUserProfile(
        @Path("username") username: String,
        @Header("Authorization") authHeader: String // JWT 토큰을 헤더에 추가
    ): Call<UserResponse>

    //중복 여부 확인
    @POST("/users/duplicate/{nickname}")
    fun checkDuplicateUser(
        @Path("nickname") nickname: String,
        @Header("Authorization") authHeader: String
    ): Call<Boolean>

    @GET("/question_post/desc") //메인페이지 질문 게시판 조회
    fun getQuestions(@Header("Authorization") token: String): Call<List<QuestionClass>>

    @GET("/mentor_post/desc") //메인페이지 구인 게시판 조회
    fun getMentors(@Header("Authorization") token: String): Call<List<MentorPostResponse>>

    @GET("/mentor_post") //메인페이지 구인 게시판 조회
    fun getMentors1(@Header("Authorization") token: String): Call<List<MentorClass>>

    @GET("/users/question_post/scrap/{username}/desc") //마이페이지 스크랩 게시판 조회
    fun getScrapQuestionInfo(
        @Path("username") questionId: String,
        @Header("Authorization") token: String
    ): Call<List<QuestionClass>>

    @GET("/users/mentor_post/scrap/{username}/desc") //마이페이지 스크랩 게시판 조회
    fun getScrapMentorInfo(
        @Path("username") questionId: String,
        @Header("Authorization") token: String
    ): Call<List<MentorClass>>

    @GET("/question_post/username/{username}") //마이페이지 내가 작성한 글 게시판 조회
    fun getMyPostsQuestionInfo(
        @Path("username") questionId: String,
        @Header("Authorization") token: String
    ): Call<List<QuestionClass>>

    @GET("/mentor_post/username/{username}") //마이페이지 내가 작성한 글 게시판 조회
    fun getMyPostsMentorInfo(
        @Path("username") questionId: String,
        @Header("Authorization") token: String
    ): Call<List<MentorClass>>



}
