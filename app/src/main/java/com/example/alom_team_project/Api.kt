package com.example.alom_team_project

import com.example.alom_team_project.model.AuthUserDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface SejongApi {
    // 세종대학교 로그인 API
    @POST("/auth?method=ClassicSession")
    fun login(@Body body: Map<String, String>): Call<SejongAuthResponse>
}

interface UserApi {
    @POST("/users/login") // 동일한 엔드포인트로 사용자 정보 전송 및 JWT 토큰 요청
    fun requestJwtToken(@Body userDTO: AuthUserDTO): Call<JwtResponse>

    @POST("/users/login") // 동일한 엔드포인트로 jwt발급 성공 여부 전송
    fun sendSuccessStatus(@Body successStatus: ServerResponse): Call<ServerResponse>

    @POST("/users/login") //프로필 변경사항 전송
    fun updateProfile(@Body nickname: Map<String, String>): Call<Void>
}
