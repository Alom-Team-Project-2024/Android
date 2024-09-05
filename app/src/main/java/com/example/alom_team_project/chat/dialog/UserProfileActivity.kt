package com.example.alom_team_project.chat.dialog

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.chat.ChatFragment
import com.example.alom_team_project.chat.ChatService
import com.example.alom_team_project.chat.UserResponse
import com.example.alom_team_project.databinding.ActivityUserProfileBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create

class UserProfileActivity() : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nickname : String = intent.getStringExtra("chatTitle")!!
        Log.d("chatTitleU", "$nickname")

        setUserInfo(nickname)

        // 뒤로 가기 버튼, 확인 버튼 클릭 시 다시 채팅방으로 돌아가기
        binding.backIcon.setOnClickListener {
            openChatPage()
        }
        binding.okBtn.setOnClickListener {
            openChatPage()
        }
    }

    private fun openChatPage() {
        val fragment = ChatFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.chatPage, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun setUserInfo(nickname: String) {
        val token = getJwtToken()

        if (token != null && nickname != null) {
            val userService = RetrofitClient.instance.create(ChatService::class.java)
            val call = userService.getUserInfobyNick("Bearer $token", nickname)

            call.enqueue(object : Callback<UserResponse> {
                override fun onResponse(
                    call: Call<UserResponse>,
                    response: Response<UserResponse>
                ) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        Log.d("UserProfile", "$user")
                        if (user != null) {
                            binding.userNickname.text = user.nickname
                            val studentCodeFormatted = "${user.studentCode.toString().takeLast(2)}학번"
                            val major = user.major
                            val pipeSymbol = " | "

                            // SpannableString 생성
                            val spannable = SpannableString(" $studentCodeFormatted$pipeSymbol$major ")

                            // 파이프 문자의 시작 위치와 끝 위치 계산
                            val pipeStart = studentCodeFormatted.length
                            val pipeEnd = pipeStart + pipeSymbol.length

                            // 색상 설정
                            val color = Color.parseColor("#4825D8") // Hex 색상 코드로 색상을 설정
                            spannable.setSpan(ForegroundColorSpan(color), pipeStart + 1, pipeEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            // 텍스트 뷰에 적용
                            binding.userInfo.text = spannable

                            binding.userTem.text = "${user.nickname}님의 세통 온도"
                            binding.temNum.text = user.point.toString()

                            binding.progressBar.max = 100
                            binding.progressBar.progress = user.point.toInt()

                            Log.d("ProfileImage", "${user.profileImage}")

                            if (user.profileImage != null) {
                                val imageUrl = getAbsoluteUrl(user.profileImage)
                                Log.d("ProfileImage", "$imageUrl")

                                Glide.with(binding.userProfileImg.context)
                                    .load(imageUrl)
                                    .apply(RequestOptions.circleCropTransform()) // 원형 변환
                                    .into(binding.userProfileImg)
                            } else {
                                Glide.with(binding.userProfileImg.context)
                                    .load(R.drawable.group_282)
                                    .apply(RequestOptions.circleCropTransform()) // 원형 변환
                                    .into(binding.userProfileImg)
                            }
                        }
                    } else {
                        Log.e("UserProfile", "User Error")
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Log.e("Token Error", "null")
                }
            })
        }
    }


    private fun getJwtToken(): String? {
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }

    fun getAbsoluteUrl(relativeUrl: String): String {
        val baseUrl = "http://15.165.213.186/" // 서버의 기본 URL
        return baseUrl + relativeUrl
    }

}