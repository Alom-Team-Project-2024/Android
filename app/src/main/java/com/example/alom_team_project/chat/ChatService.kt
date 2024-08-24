package com.example.alom_team_project.chat

import com.example.alom_team_project.question_board.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatService {

    @POST("chats")
    fun chatRoom( // 채팅방 생성 : 학번 2개 요청
        @Header("Authorization") token: String, // 인증 토큰
        @Query("username1") username1: String,
        @Query("username2") username2: String
    ): Call<ChatRoomResponse> // 응답으로 ChatRoomResponse 클래스를 사용

    @GET("chats/rooms/{nickname}")
    fun getChatList( // 특정 유저가 참가 중인 채팅 목록 조회
        @Header("Authorization") token: String,
        @Path("nickname") nickname: String
    ): Call<List<ChatRoomResponse>>

    @GET("chats/id/{chat_id}")
    fun getChatRoomById( // 채팅방 아이디롤 통해 특정 채팅방 조회
        @Header("Authorization") token: String,
        @Path("chat_id") id: Long
    ): Call<ChatRoomResponse>

    @GET("users/{id}")
    fun getUserInfo( // 사용자 정보 조회
        @Header("Authorization") token: String,
        @Path("id") id: String // Path로 전송
    ): Call<User>

    @GET("chats/room/{chatRoomId}/messages")
    fun getChatHistory(
        @Header("Authorization") token: String,
        @Path("chatRoomId") chatRoomId: Int
    ): Call<List<ChatHistoryResponse>>

}