package com.example.alom_team_project.job_board

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.chat.ChatFragment
import com.example.alom_team_project.chat.ChatListActivity
import com.example.alom_team_project.chat.ChatRoomResponse
import com.example.alom_team_project.chat.ChatService
import com.example.alom_team_project.databinding.FragmentMentorDetailBinding
import com.example.alom_team_project.mypage.UserResponse
import com.example.alom_team_project.question_board.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MentorDetailFragment : Fragment() {

    //새로고침
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private val refreshInterval: Long = 500 // 0.5초

    private var _binding: FragmentMentorDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var mentorService: MentorPostService
    private var isScrapped = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMentorDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAutoRefresh()

        // 뒤로가기 버튼 설정

        binding.backIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Retrofit 초기화
        mentorService = RetrofitClient.instance.create(MentorPostService::class.java)

        // Arguments에서 멘토게시물Id 받기
        val mentorId = arguments?.getLong("MENTOR_ID")
        //Log.d("DetailFragment", "MENTOR ID: $mentorId")

        binding.chatBtn.setOnClickListener {
            createChatRoom()
        }

        // 스크랩 상태 초기화
        if (mentorId != null) {
            isScrapped = getScrapStatus(mentorId)
            updateScrapButtonUI(isScrapped)
        }

        // 스크랩 버튼 클릭 이벤트
        binding.scrap.setOnClickListener {
            val username = getUsername()
            mentorId?.let { id ->
                if (username != null && !isScrapped) {
                    postScrap(username, id)
                } else {
                    Toast.makeText(requireContext(), "이미 스크랩을 완료했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 구인 정보 가져오기
        if (mentorId != null) {
            fetchMentorDetails(mentorId) { writer ->
            }
        }
    }

    private fun createChatRoom() {
        val token = getJwtToken()
        val username = getUsername()
        val mentorId = arguments?.getLong("MENTOR_ID")

        if (token != null && username != null && mentorId != null) {
            fetchMentorDetails(mentorId) { user2nick ->
                if (!user2nick.isNullOrEmpty()) {
                    fetchNickname(token, username) { user1nick ->
                        if (!user1nick.isNullOrEmpty() && user1nick != user2nick) {
                            val chatService = RetrofitClient.instance.create(ChatService::class.java)
                            val call = chatService.chatRoom("Bearer $token", user1nick, user2nick)

                            call.enqueue(object : Callback<ChatRoomResponse> {
                                override fun onResponse(call: Call<ChatRoomResponse>, response: Response<ChatRoomResponse>) {
                                    if (response.isSuccessful) {
                                        response.body()?.let {
                                            openChatPage(it.id)
                                        } ?: run {
                                            Log.e("API Error", "ChatRoomResponse is null")
                                        }
                                    } else {
                                        Log.e("API Error", "Response Code: ${response.code()}, Message: ${response.message()}")
                                    }
                                }

                                override fun onFailure(call: Call<ChatRoomResponse>, t: Throwable) {
                                    Log.e("API Error", "Failure: ${t.message}")
                                }
                            })
                        } else {
                            Log.e("Nickname Error", "Failed to fetch nickname or usernames are identical.")
                            Toast.makeText(requireContext(), "Cannot create chat room.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("Mentor Error", "Failed to fetch mentor details or user2nick is null.")
                    Toast.makeText(requireContext(), "Cannot fetch mentor details.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.e("Token or MentorId Error", "Token or MentorId is null.")
            Toast.makeText(requireContext(), "Invalid token or mentor ID.", Toast.LENGTH_SHORT).show()
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
                        Log.e("MentorPostU", "Failed to fetch user profile. Response code: ${response.code()}")
                        callback(null)
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Log.e("MentorPostU", "Network Error: ${t.message}")
                    callback(null)
                }
            })
    }

    private fun openChatPage(chatRoomId: Long) {
        val fragment = ChatFragment()
        val bundle = Bundle()
        bundle.putLong("ChatRoomId", chatRoomId)
        fragment.arguments = bundle
        Log.d("ChatListActivity", "Opening ChatPage with ChatRoom ID: $chatRoomId")

        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // 프래그먼트를 전환 (replace)하거나 추가 (add)합니다.
        fragmentTransaction.replace(R.id.mentorPostPage, fragment)
        // 뒤로가기 스택에 추가
        fragmentTransaction.addToBackStack(null)

        // 전환 실행
        fragmentTransaction.commit()
    }

    private fun getJwtToken(): String {
        val sharedPref = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", "") ?: ""
    }

    private fun getUsername(): String? {
        val sharedPref = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPref.getString("username", null)
    }

    private fun fetchMentorDetails(mentorId: Long, callback: (String?) -> Unit) {
        val token = getJwtToken()

        mentorService.getMentorFromId("Bearer $token", mentorId).enqueue(object : Callback<MentorClass> {
            override fun onResponse(call: Call<MentorClass>, response: Response<MentorClass>) {
                if (response.isSuccessful) {
                    response.body()?.let { mentor ->
                        bindMentorToViews(mentor)
                        callback(mentor.writer) // 콜백을 통해 mentor.writer 전달
                    } ?: run {
                        callback(null) // mentor가 null인 경우
                    }
                } else {
                    Toast.makeText(requireContext(), "불러오기 실패", Toast.LENGTH_SHORT).show()
                    callback(null) // 실패 시 null 전달
                }
            }

            override fun onFailure(call: Call<MentorClass>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                callback(null) // 오류 발생 시 null 전달
            }
        })
    }


    private fun bindMentorToViews(mentor: MentorClass) {
        // 질문 내용 설정
        binding.postContent.text = mentor.text

        // 좋아요 수, 댓글 수, 스크랩 수 설정
        binding.scrapCount.text = mentor.scrapCount.toString()

        val username = mentor.username
        //질문자 프로필 설정
        fetchUpdateUserInfo(username)
    }

    private fun fetchUpdateUserInfo(username: String) {
        val token = getJwtToken()

        // 프로필 정보 가져오기 요청
        mentorService.getProfile("Bearer $token", username).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    // 성공적으로 사용자 프로필 정보를 받았을 때 처리
                    response.body()?.let { user ->
                        // 사용자 닉네임을 UI에 설정
                        binding.postUserId.text = user.nickname


                        if (!user.profileImage.isNullOrEmpty()) {
                            val fullImageUrl = "http://15.165.213.186/" + user.profileImage
                            Glide.with(binding.root.context)
                                .load(fullImageUrl)
                                .into(binding.postProfile)
                        } else {
                            // 프로필 이미지가 없을 경우 기본 이미지 설정
                            binding.postProfile.setImageResource(R.drawable.group_172)
                        }
                    }
                } else {
                    // 요청이 실패했을 때 처리 (예: 에러 메시지 출력)
                    Log.e("UserProfile", "Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                // 네트워크 오류나 다른 문제가 발생했을 때 처리
                Log.e("UserProfile", "Request failed", t)
            }
        })
    }

    private fun postScrap(username: String, mentorId: Long) {
        val token = getJwtToken()

        mentorService.scrapPost("Bearer $token", username, mentorId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    isScrapped = true
                    saveScrapStatus(mentorId, true)
                    updateScrapButtonUI(isScrapped)
                    Toast.makeText(requireContext(), "스크랩을 완료했습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "스크랩을 완료하는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateScrapButtonUI(isScrapped: Boolean) {
        if (isScrapped) {
            binding.scrap.setImageResource(R.drawable.scrap_button2)  // 스크랩된 상태의 이미지
            binding.scrap.isEnabled = false  // 스크랩 버튼 비활성화
        } else {
            binding.scrap.setImageResource(R.drawable.scrap_button)  // 기본 스크랩 이미지
            binding.scrap.isEnabled = true  // 스크랩 버튼 활성화
        }
    }

    private fun saveScrapStatus(mentorId: Long, isScrapped: Boolean) {
        val sharedPref = requireContext().getSharedPreferences("scraps", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("post_$mentorId", isScrapped)
            apply()
        }
    }

    private fun getScrapStatus(mentorId: Long): Boolean {
        val sharedPref = requireContext().getSharedPreferences("scraps", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("post_$mentorId", false)
    }

    private fun setupAutoRefresh() {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val mentorId = arguments?.getLong("MENTOR_ID")
                if (mentorId != null) {
                    fetchMentorDetails(mentorId) { writer ->
                    }
                }
                handler.postDelayed(this, refreshInterval)
            }
        }
        handler.post(runnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacks(runnable)
    }
}
