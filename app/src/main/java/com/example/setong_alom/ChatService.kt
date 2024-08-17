package com.example.setong_alom

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatService {

    @POST("chats") // 엔드포인트
    fun chatRoom( // 채팅방 생성
        @Header("Authorization") token: String, // 인증 토큰
        @Query("id1") id1: String, // id1을 쿼리 파라미터로 전송
        @Query("id2") id2: String  // id2를 쿼리 파라미터로 전송
    ): Call<ChatRoomResponse> // 응답으로 ChatRoomResponse 클래스를 사용

    @GET("chats/rooms/{nickname}")
    fun getChatList( // 채팅 목록 조회
        @Header("Authorization") token: String,
        @Path("nickname") nickname: String
    ): Call<List<ChatRoomResponse>>

    @GET("users/{id}")
    fun getUserInfo( // 사용자 정보 조회
        @Header("Authorization") token: String,
        @Path("id") id: String // Path로 전송
    ): Call<User>
}