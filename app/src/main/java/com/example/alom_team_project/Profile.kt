package com.example.alom_team_project

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    private lateinit var etNickname: EditText
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        etNickname = findViewById(R.id.et_nickname)
        btnSubmit = findViewById(R.id.btn_submit)

        btnSubmit.setOnClickListener {
            val nickname = etNickname.text.toString().trim()
            val studentId = getStudentId()

            updateProfile(nickname, studentId)
        }
    }

    private fun getStudentId(): String? {
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        return sharedPref.getString("student_id", null)
    }

    private fun updateProfile(nickname: String, studentId: String?) {
        if (studentId == null) {
            Toast.makeText(this, "학번 불러오기 실패.", Toast.LENGTH_SHORT).show()
            return
        }

        val profileData = mapOf("username" to studentId, "name" to nickname)

        RetrofitClient.userApi.updateProfile(profileData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 성공 처리
                    Toast.makeText(this@ProfileActivity, "프로필 수정 성공", Toast.LENGTH_SHORT).show()
                } else {
                    // 실패 처리
                    Toast.makeText(this@ProfileActivity, "프로필 수정 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // 오류 처리
                Toast.makeText(this@ProfileActivity, "오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
