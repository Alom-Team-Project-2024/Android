package com.example.setong_alom

import ChatListAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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

        // 검색 필터
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트가 변경될 때마다 어댑터의 필터링 메소드 호출
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed
            }
        })

        // 클릭 리스너 설정
        adapter.setOnItemClickListener(object : ChatListAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val clickedItem = chatList[position]
                val intent = Intent(this@ChatListActivity, ChatActivity::class.java)
                startActivity(intent)
            }
        })
    }

    private fun fetchChatList() {
        // 토큰 가져오기
        val token = TokenManager.getToken(this)
        val nickname = "user760" // 사용자(내) 닉네임

        // 토큰이 null인지 확인
        if (token != null) {
            val chatService = RetrofitClient.instance.create(ChatService::class.java)
            val call = chatService.getChatList(token, nickname)

            call.enqueue(object : Callback<List<ChatRoomResponse>> {
                override fun onResponse(call: Call<List<ChatRoomResponse>>, response: Response<List<ChatRoomResponse>>) {
                    if (response.isSuccessful) {
                        val chatRooms = response.body()
                        if (chatRooms != null) {
                            Log.d("Chat", "success")

                            Log.d("Chat", "Response Code: ${response.code()}")
                            Log.d("Chat", "Response Body: ${response.body()}")

                            chatList.clear() // 기존 리스트를 비웁니다.
                            chatRooms.forEach { chatRoom ->
                                chatList.add(ChatList(profile = R.drawable.profile, name = chatRoom.chatRoomName, content = "안녕하세요", time = "1분 전"))
                            }
                            adapter.notifyDataSetChanged() // 데이터 변경을 어댑터에 알립니다.
                            adapter.filter("")
                        } else {
                            Log.e("Chat", "No chat rooms found.")
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
            // 토큰이 null인 경우 처리
            Log.e("Token Error", "Token is null. Cannot fetch chat list.")
        }
    }
}
