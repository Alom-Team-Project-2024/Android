package com.example.setong_alom.chatlist

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.setong_alom.chat.ChatRoomResponse
import com.example.setong_alom.chat.ChatService
import com.example.setong_alom.R
import com.example.setong_alom.RetrofitClient
import com.example.setong_alom.chat.ChatFragment
import com.example.setong_alom.databinding.ActivityChatListBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatListBinding
    private lateinit var adapter: ChatListAdapter
    private var chatList = ArrayList<ChatList>() // 채팅 목록

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ChatListAdapter(chatList)
        binding.rcvChattinglist.adapter = adapter
        binding.rcvChattinglist.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // 채팅 목록 불러오기
        fetchChatList()

        // 검색 필터 설정
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 클릭 리스너 설정
        adapter.setOnItemClickListener(object : ChatListAdapter.OnItemClickListener {
            override fun onItemClick(chatRoomId: Long) {
                openChatPage(chatRoomId)
                Log.d("ChatList", "$chatRoomId")
            }
        })
    }

    private fun fetchChatList() {
        val token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6IjIzMDExNjc2Iiwicm9sZSI6IlVTRVIiLCJuaWNrbmFtZSI6InVzZXI0OTciLCJpYXQiOjE3MjQ1MjIwMjIsImV4cCI6MTcyNDUyMzgyMn0.qtoPzhPkIA6NBlUsHaI1HqB6X3qXwaC3gnw8K72J8B8"
        val nickname = "user497" // 사용자(내) 닉네임

        if (token != null) {
            val chatService = RetrofitClient.instance.create(ChatService::class.java)
            val call = chatService.getChatList(token, nickname)

            call.enqueue(object : Callback<List<ChatRoomResponse>> {
                override fun onResponse(call: Call<List<ChatRoomResponse>>, response: Response<List<ChatRoomResponse>>) {
                    if (response.isSuccessful) {
                        val chatRooms = response.body()
                        if (chatRooms != null) {
                            chatList.clear() // 기존 리스트 비우기

                            chatRooms.forEach { chatRoom ->
                                val chatRoomId = chatRoom.id

                                val nicknames: List<String> = chatRoom.userResponseList.map { it.nickname }
                                val profileImages: List<String> = chatRoom.userResponseList.map { it.profileImage }

                                val firstUsername: String? = nicknames.getOrNull(0)
                                val secondUsername: String? = nicknames.getOrNull(1)

                                val firstUserProfile: String? = profileImages.getOrNull(0)
                                val secondUserProfile: String? = profileImages.getOrNull(1)

                                val chatTitle: String?
                                val profile: Int?

                                if (nickname == firstUsername) {
                                    chatTitle = secondUsername
                                    profile = R.drawable.profile // 여기에 실제 프로필 이미지를 사용할 수 있습니다.
                                } else {
                                    chatTitle = firstUsername
                                    profile = R.drawable.profile // 여기에 실제 프로필 이미지를 사용할 수 있습니다.
                                }

                                if (chatTitle != null) {
                                    chatList.add(
                                        ChatList(
                                            chatRoomId = chatRoomId, // 채팅방 ID 추가
                                            profile = profile,
                                            name = chatTitle,
                                            content = "안녕하세요",
                                            time = "1분 전"
                                        )
                                    )
                                }
                            }
                            adapter.notifyDataSetChanged() // 데이터 변경을 어댑터에 알리기
                            adapter.filter("")
                        } else {
                            Log.e("ChatList", "No chat rooms found.")
                        }
                    } else {
                        Log.e("API Error", "Response Code: ${response.code()}, Message: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<List<ChatRoomResponse>>, t: Throwable) {
                    Log.e("API Error", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("Token Error", "Token is null. Cannot fetch chat list.")
        }
    }

    private fun openChatPage(chatRoomId: Long) {
        val fragment = ChatFragment()
        val bundle = Bundle()
        bundle.putLong("ChatRoomId", chatRoomId)
        fragment.arguments = bundle
        Log.d("ChatListActivity", "Opening ChatPage with ChatRoom ID: $chatRoomId")


        supportFragmentManager.beginTransaction()
            .replace(R.id.chatPage, fragment)
            .addToBackStack(null)
            .commit()
    }

}

