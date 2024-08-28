package com.example.alom_team_project.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.alom_team_project.databinding.ActivityLoginBinding
import com.example.alom_team_project.MainActivity
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.mypage.MypageActivity
import com.example.alom_team_project.mypage.ProfileActivity
import com.example.alom_team_project.mypage.ScrapBoardActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val id = binding.etId.text.toString()
            val pw = binding.etPassword.text.toString()

            val body = mapOf(
                "id" to id,
                "pw" to pw
            )

            // 로그인 요청
            RetrofitClient.sejongApi.login(body).enqueue(object : Callback<SejongAuthResponse> {
                override fun onResponse(call: Call<SejongAuthResponse>, response: Response<SejongAuthResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val auth = response.body()?.result
                        if (auth?.isAuth == "true") {
                            val userData = auth.body
                            requestJwtToken(userData, id)
                        } else {
                            showError("아이디나 비밀번호가 일치하지 않습니다.")
                        }
                    } else {
                        showError("네트워크 문제로 로그인하지 못했습니다. 다시 시도하시겠습니까?")
                    }
                }

                override fun onFailure(call: Call<SejongAuthResponse>, t: Throwable) {
                    showError("네트워크 문제로 로그인하지 못했습니다. 다시 시도하시겠습니까?")
                }
            })
        }
    }

    private fun requestJwtToken(userData: SejongAuthResponseResultBodyJson, id: String) {
        // 한국어 상태 값을 영어로 매핑
        val statusMap = mapOf(
            "재학" to RegistrationStatus.ATTENDING,
            "휴학" to RegistrationStatus.TAKEOFFSCHOOL,
            "졸업" to RegistrationStatus.GRADUATE
        )

        // 상태 값을 매핑
        val registrationStatus = statusMap[userData.status]
            ?: throw IllegalArgumentException("Unknown registration status: ${userData.status}")

        // AuthUserDTO 생성
        val userDTO = AuthUserDTO(
            username = id,  // ID를 username으로 사용
            name = userData.name,
            major = userData.major,
            studentGrade = userData.grade,
            registrationStatus = registrationStatus
        )

        // JWT 요청
        RetrofitClient.userApi.requestJwtToken(userDTO).enqueue(object : Callback<JwtResponse> {
            override fun onResponse(call: Call<JwtResponse>, response: Response<JwtResponse>) {
                if (response.isSuccessful) {
                    // 응답 헤더에서 JWT 추출
                    val authHeader = response.headers().get("Authorization")
                    val token = authHeader?.removePrefix("Bearer ")

                    if (!token.isNullOrEmpty()) {
                        // JWT와 username 저장
                        saveUserData(token, id)
                        JwtProvider.setToken(token)
                        //navigateToMainActivity()
                        //navigateToScrapActivity()
                        //navigateToMypageActivity()
                        navigateToProfileActivity()  // 프로필 설정 페이지 이동
                    } else {
                        showError("네트워크 문제로 로그인하지 못했습니다. 다시 시도하시겠습니까?")
                    }
                } else {
                    showError("네트워크 문제로 로그인하지 못했습니다. 다시 시도하시겠습니까?")
                }
            }

            override fun onFailure(call: Call<JwtResponse>, t: Throwable) {
                Log.e("JWT_REQUEST", "JWT 발급 오류", t)
                showError("네트워크 문제로 로그인하지 못했습니다. 다시 시도하시겠습니까?")
            }
        })
    }

    private fun saveUserData(token: String, username: String) {
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("jwt_token", token)
            putString("username", username)
            apply() // 비동기적으로 변경사항을 저장
        }
    }

    private fun navigateToProfileActivity() {
        val intent = Intent(this@LoginActivity, ProfileActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMypageActivity() {
        val intent = Intent(this@LoginActivity, MypageActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToScrapActivity() {
        val intent = Intent(this@LoginActivity, ScrapBoardActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
    }
}

