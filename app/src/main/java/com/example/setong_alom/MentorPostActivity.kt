package com.example.setong_alom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.setong_alom.databinding.ActivityMentorPostBinding
import com.example.setong_alom.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MentorPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMentorPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMentorPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 예시로 토큰을 저장
        val token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6IjIyMDExMzE1Iiwicm9sZSI6IlVTRVIiLCJuaWNrbmFtZSI6InVzZXIzMzciLCJpYXQiOjE3MjM3MzY4MjAsImV4cCI6MTcyMzczODYyMH0.EJhTKErVq2vZ0qp3xDR9BQEOuHBdjqSHlKZ6wCNKSmo"
        TokenManager.saveToken(this, token)

        binding.chatBtn.setOnClickListener {
            createChatRoom()
        }
    }

    private fun createChatRoom() {
        // 토큰 가져오기
        val token = TokenManager.getToken(this)

        // 사용자 ID 설정
        val user1Id = "1"  // 현재 사용자 ID
        val user2Id = "2"  // 게시글 작성자 ID

        // 토큰이 null인지 확인
        if (token != null) {
            val chatService = RetrofitClient.instance.create(ChatService::class.java)
            val call = chatService.chatRoom(token, user1Id, user2Id)

            call.enqueue(object : Callback<ChatRoomResponse> {
                override fun onResponse(call: Call<ChatRoomResponse>, response: Response<ChatRoomResponse>) {
                    if (response.isSuccessful) {
                        // 성공적인 응답 처리
                        val chatRoomName = response.body()?.chatRoomName
                        Log.d("Chat", "ChatRoomName: $chatRoomName")

                        // 챗액티비티로 이동
                        val intent = Intent(this@MentorPostActivity, ChatActivity::class.java)
                        startActivity(intent)
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
