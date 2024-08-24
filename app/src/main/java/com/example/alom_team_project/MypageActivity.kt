package com.example.alom_team_project

import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MypageActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvCodeMajor: TextView
    private lateinit var tvNamePoint: TextView
    private lateinit var tvPoint: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        // UI 요소 초기화
        tvName = findViewById(R.id.tv_name)
        tvCodeMajor = findViewById(R.id.tv_code_major)
        tvNamePoint = findViewById(R.id.tv_name_point)
        tvPoint = findViewById(R.id.tv_point)
        progressBar = findViewById(R.id.progressBar2)

        // 사용자 정보를 조회하는 함수 호출
        getUserProfile()
    }

    private fun getUserProfile() {
        val api = RetrofitClient.userApi // RetrofitClient 사용
        val token = getJwtToken()
        val username = getUsername()

        if (token.isNullOrEmpty() || username.isNullOrEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        api.getUserProfile(username, "Bearer $token").enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        // UI에 데이터 반영
                        tvName.text = user.name

                        // studentCode를 23학번 형태로 변환
                        val studentCodeFormatted = "${user.studentCode.toString().takeLast(2)}학번"
                        tvCodeMajor.text = "$studentCodeFormatted\n${user.major}"

                        // 포인트 관련 데이터 설정
                        tvNamePoint.text = "${user.name}님의 세통 온도"
                        tvPoint.text = user.point.toString()

                        // ProgressBar의 최대 값과 현재 값 설정
                        progressBar.max = 100 // 최대 값은 100으로 설정
                        progressBar.progress = user.point.toInt() // Double을 Int로 변환하여 설정
                    }
                } else {
                    Toast.makeText(this@MypageActivity, "사용자 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("UserProfile", "Network Error: ${t.message}")
                Toast.makeText(this@MypageActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getJwtToken(): String? {
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }

    private fun getUsername(): String? {
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        return sharedPref.getString("username", null)
    }
}
