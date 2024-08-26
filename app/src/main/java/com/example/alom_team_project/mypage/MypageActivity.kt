package com.example.alom_team_project.mypage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MypageActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvCodeMajor: TextView
    private lateinit var tvNamePoint: TextView
    private lateinit var tvPoint: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnMyPosts: Button
    private lateinit var tvMoreScrap: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        // UI 요소 초기화
        tvName = findViewById(R.id.tv_name)
        tvCodeMajor = findViewById(R.id.tv_code_major)
        tvNamePoint = findViewById(R.id.tv_name_point)
        tvPoint = findViewById(R.id.tv_point)
        progressBar = findViewById(R.id.progressBar2)
        btnMyPosts = findViewById(R.id.button2)
        tvMoreScrap = findViewById(R.id.tv_more)
        btnLogout = findViewById(R.id.btn_logout)

        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // 버튼 클릭 시 MyPostActivity로 이동
        btnMyPosts.setOnClickListener {
            val intent = Intent(this, MyPostsActivity::class.java)
            startActivity(intent)
        }

        // 텍스트뷰 클릭 시 ScrapBoardActivity로 이동
        tvMoreScrap.setOnClickListener {
            val intent = Intent(this, ScrapBoardActivity::class.java)
            startActivity(intent)
        }

        // 로그아웃 버튼 클릭 시 로그인 페이지로 이동
        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // 사용자 정보를 조회하는 함수 호출
        getUserProfile()
        progressBar.progress = 100
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
                        progressBar.setProgress(user.point.toInt())  // Double을 Int로 변환하여 설정

                        // 로그 추가
                        Log.d("MypageActivity", "ProgressBar max: ${progressBar.max}, current progress: ${progressBar.progress}, user point: $user.point")
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
