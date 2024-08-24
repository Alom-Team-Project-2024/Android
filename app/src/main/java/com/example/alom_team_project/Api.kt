package com.example.alom_team_project

import QuestionPostResponse
import com.example.alom_team_project.model.AuthUserDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
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

    @POST("/users/{username}/profile-image") // 프로필 이미지 업로드
    fun uploadProfileImage(
        @Path("username") username: String, // 학번 경로 파라미터
        @Header("Authorization") authHeader: String, // JWT 토큰을 헤더에 추가
        @retrofit2.http.Part file: okhttp3.MultipartBody.Part // 업로드할 파일
    ): Call<String> // 응답을 String으로 받음

    @GET("/users/username/{username}") // 사용자 정보 조회
    fun getUserProfile(
        @Path("username") username: String,
        @Header("Authorization") authHeader: String // JWT 토큰을 헤더에 추가
    ): Call<UserResponse>


    @GET("/question_post") //메인페이지 질문 게시판 조회
    fun getQuestions(@Header("Authorization") token: String): Call<List<QuestionPostResponse>>

    @GET("/mentor_post") //메인페이지 구인 게시판 조회
    fun getMentors(@Header("Authorization") token: String): Call<List<MentorPostResponse>>

    // 스크랩된 질문들을 가져오는 API
    @GET("/scrap-questions") // 엔드포인트를 실제 URL로 변경하세요
    fun getScrapQuestions(@Header("Authorization") token: String): Call<List<QuestionPostResponse>>

    //중복 여부 확인
    @POST("/users/duplicate/{nickname}")
    fun checkDuplicateUser(
        @Path("nickname") nickname: String,
        @Header("Authorization") authHeader: String
    ): Call<Void>
}
