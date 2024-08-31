package com.example.alom_team_project.chat

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.databinding.ActivityChatListBinding
import com.example.alom_team_project.home.NavigationFragment
import com.example.alom_team_project.mypage.UserResponse
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
                hideKeyboard()
                openChatPage(chatRoomId)
                Log.d("ChatList", "$chatRoomId")
            }
        })

        binding.btmNav.setOnClickListener {
            openNavFragment()
        }

        binding.root.setOnClickListener {
            hideKeyboard()
        }
    }

    private fun fetchChatList() {
        val token = getJwtToken()
        val username = getUsername()

        if (token != null && username != null) {
            fetchNickname(token, username) { nickname ->
                if (nickname != null) {
                    // nickname 저장
                    val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("nickname", nickname)
                        apply()
                    }

                    fetchChatRooms(token, nickname)
                } else {
                    Log.e("ChatList", "Failed to fetch nickname.")
                }
            }
        } else {
            Log.e("Token Error", "Token or username is null. Cannot fetch chat list.")
        }
    }

    private fun fetchNickname(token: String, username: String, callback: (String?) -> Unit) {
        RetrofitClient.userApi.getUserProfile(username, "Bearer $token")
            .enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        val nickname = user?.nickname
                        callback(nickname)
                    } else {
                        Log.e("ChatList", "Failed to fetch user profile. Response code: ${response.code()}")
                        callback(null)
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Log.e("ChatList", "Network Error: ${t.message}")
                    callback(null)
                }
            })
    }

    private fun fetchChatRooms(token: String, nickname: String) {
        val chatService = RetrofitClient.instance.create(ChatService::class.java)
        val call = chatService.getChatList("Bearer $token", nickname)

        call.enqueue(object : Callback<List<ChatRoomResponse>> {
            override fun onResponse(call: Call<List<ChatRoomResponse>>, response: Response<List<ChatRoomResponse>>) {
                if (response.isSuccessful) {
                    val chatRooms = response.body()
                    Log.d("chatRoomResponse", "$chatRooms")
                    if (chatRooms != null) {
                        chatList.clear() // 이전 채팅 목록 초기화

                        val fetchCount = chatRooms.size
                        var processedCount = 0

                        chatRooms.forEach { chatRoom ->
                            val chatRoomId = chatRoom.id
                            val nicknames = chatRoom.userResponseList.map { it.nickname }
                            val profileImages = chatRoom.userResponseList.map { it.profileImage }

                            val firstUsername = nicknames.getOrNull(0)
                            val secondUsername = nicknames.getOrNull(1)

                            val firstUserProfile = profileImages.getOrNull(0)
                            val secondUserProfile = profileImages.getOrNull(1)

                            val chatTitle: String?
                            var profileImg: String? = null

                            if (nickname == firstUsername) {
                                chatTitle = secondUsername
                                profileImg = secondUserProfile
                            } else {
                                chatTitle = firstUsername
                                profileImg = firstUserProfile
                            }
                            Log.d("ChatlistAct", "$chatTitle, $profileImg")

                            fetchLastChatMessage(token, chatRoomId, nickname) { chatData ->
                                chatData?.let { (message, timestamp) ->
                                    Log.d("Last Message", "Message: $message, Time: $timestamp")

                                    // 메시지나 타임스탬프가 비어있는 경우는 가장 최신 메시지로 간주
                                    val effectiveTimestamp = if (timestamp.isNotEmpty()) timestamp else "9999-12-31T23:59:59" // 가상의 미래 타임스탬프

                                    chatList.add(
                                        ChatList(
                                            chatRoomId = chatRoomId,
                                            profile = profileImg,
                                            name = chatTitle ?: "",
                                            content = message,
                                            time = effectiveTimestamp
                                        )
                                    )

                                } ?: run {
                                    Log.d("Last Message", "No message found or error occurred.")

                                    // 메시지가 없을 때에도 이름과 프로필 사진은 추가
                                    chatList.add(
                                        ChatList(
                                            chatRoomId = chatRoomId,
                                            profile = profileImg,
                                            name = chatTitle ?: "",
                                            content = "",
                                            time = "9999-12-31T23:59:59" // 가상의 미래 타임스탬프
                                        )
                                    )
                                }

                                processedCount++
                                if (processedCount == fetchCount) {
                                    // 처리된 메시지 수가 전체 메시지 수와 같을 때 정렬 및 업데이트
                                    chatList.sortByDescending { it.time } // 최신 메시지 순으로 정렬
                                    adapter.notifyDataSetChanged() // 데이터 변경을 어댑터에 알리기
                                    adapter.filter("")
                                }
                            }
                        }
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
    }




    // 마지막 채팅 메시지 불러오기
    private fun fetchLastChatMessage(token: String, chatRoomId: Long, nickname: String, callback: (Pair<String, String>?) -> Unit) {
        if (token.isNotEmpty()) {
            val chatHistory = RetrofitClient.instance.create(ChatService::class.java)
            val call = chatHistory.getChatHistory("Bearer $token", chatRoomId)

            call.enqueue(object : Callback<List<ChatHistoryResponse>> {
                override fun onResponse(call: Call<List<ChatHistoryResponse>>, response: Response<List<ChatHistoryResponse>>) {
                    if (response.isSuccessful) {
                        val chatMessages = response.body()
                        if (chatMessages != null && chatMessages.isNotEmpty()) {
                            // 마지막 메시지와 보낸 시각 가져오기
                            val lastChatHistoryResponse = chatMessages.last()
                            val message = lastChatHistoryResponse.message // 마지막 메시지 가져오기
                            val timestamp = lastChatHistoryResponse.createdAt // 보낸 시각 가져오기

                            val chatData = Pair(message, timestamp) // Pair로 메시지와 타임스탬프 묶기
                            callback(chatData) // 콜백으로 전달
                        } else {
                            callback(null) // 메시지가 없으면 null 반환
                        }
                    } else {
                        Log.e("API Error", "Response Code: ${response.code()}, Message: ${response.message()}")
                        callback(null) // API 오류 시 null 반환
                    }
                }

                override fun onFailure(call: Call<List<ChatHistoryResponse>>, t: Throwable) {
                    Log.e("API Error", "Failure: ${t.message}")
                    callback(null) // 요청 실패 시 null 반환
                }
            })
        } else {
            Log.e("Token Error", "Token is empty")
            callback(null) // 토큰이 없으면 null 반환
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

    private fun getJwtToken(): String? {
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }

    private fun getUsername(): String? {
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        return sharedPref.getString("username", null)
    }

    private fun openNavFragment() {
        // 네비게이션 프래그먼트
        val fragment = NavigationFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.chat_nav, fragment)
            .commit()
    }

    // 화면 터치 시 키보드 내리기
    private fun hideKeyboard() {
        if (this != null && this.currentFocus != null) {
            val inputManager: InputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(this.currentFocus?.windowToken,InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}