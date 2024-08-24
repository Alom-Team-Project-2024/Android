package com.example.setong_alom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.setong_alom.chat.ChatRoomResponse
import com.example.setong_alom.chat.ChatService
import com.example.setong_alom.databinding.ActivityMentorPostBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MentorPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMentorPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMentorPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chatBtn.setOnClickListener {
            createChatRoom()
        }
    }

    private fun createChatRoom() {
        // 토큰 가져오기
        val token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6IjIzMDExNjc2Iiwicm9sZSI6IlVTRVIiLCJuaWNrbmFtZSI6InVzZXI0OTciLCJpYXQiOjE3MjQ1MjIwMjIsImV4cCI6MTcyNDUyMzgyMn0.qtoPzhPkIA6NBlUsHaI1HqB6X3qXwaC3gnw8K72J8B8"

        // 사용자 ID 설정
        val user1name = "23011676"  // 현재 사용자 학번 (처음 로그인 시 저장하기)
        val user2name = "22011315"  // 게시글 작성자 학번 (게시물 조회 시 저장하기)

        // 토큰이 null인지 확인
        if (token != null) {
            val chatService = RetrofitClient.instance.create(ChatService::class.java)
            val call = chatService.chatRoom(token, user1name, user2name)

            call.enqueue(object : Callback<ChatRoomResponse> {
                override fun onResponse(call: Call<ChatRoomResponse>, response: Response<ChatRoomResponse>) {
                    if (response.isSuccessful) {
                        val chatRoomResponse = response.body()

                        // chatRoomResponse가 null이 아닌 경우 id 가져오기
                        chatRoomResponse?.let {
                            val chatRoomId = it.id
                            Log.d("ChatRoom ID", "ChatRoom ID: $chatRoomId")

                            // 챗화면으로 이동
                        } ?: run {
                            Log.e("API Error", "ChatRoomResponse is null")
                        }
                    } else {
                        // 응답 실패 처리
                        Log.e("API Error", "Response Code: ${response.code()}, Message: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ChatRoomResponse>, t: Throwable) {
                    // 요청 실패 처리
                    Log.e("API Error", "Failure: ${t.message}")
                }
            })
        } else {
            // 토큰이 null인 경우 처리
            Log.e("Token Error", "Token is null. Cannot create chat room.")
        }
    }
}
