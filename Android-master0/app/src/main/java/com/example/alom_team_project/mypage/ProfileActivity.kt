package com.example.alom_team_project.mypage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.alom_team_project.MainActivity
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var etNickname: EditText
    private lateinit var btnSubmit: Button
    private lateinit var imgProfile: ImageView
    private lateinit var btnProfile: ImageButton

    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        etNickname = findViewById(R.id.et_nickname)
        btnSubmit = findViewById(R.id.btn_submit)
        imgProfile = findViewById(R.id.img_profile)
        btnProfile = findViewById(R.id.btn_profile)

        // 기본 이미지 설정
        imgProfile.setBackgroundResource(R.drawable.group_236)

        btnProfile.setOnClickListener {
            openGallery() // 갤러리 열기
        }

        btnSubmit.setOnClickListener {
            val nickname = etNickname.text.toString().trim()
            val token = getJwtToken()
            val username = getUsername()

            if (nickname.isNotEmpty() && token != null && username != null) {
                updateProfile(nickname, username, token)
                if (selectedImageUri != null) {
                    uploadProfileImage(selectedImageUri!!, username, token)
                }
            } else {
                Toast.makeText(this, "닉네임, 학번 또는 토큰을 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            imgProfile.setImageURI(selectedImageUri) // 선택된 이미지를 이미지 프로필에 표시
        }
    }

    private fun getJwtToken(): String? {
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }

    private fun getUsername(): String? {
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        return sharedPref.getString("username", null)
    }

    private fun updateProfile(nickname: String, username: String, token: String) {
        val profileData = mapOf("username" to username, "nickname" to nickname)
        val authHeader = "Bearer $token"

        RetrofitClient.userApi.updateProfile(authHeader, profileData).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val updateMessage = response.body()
                    if (updateMessage == null) {
                        Toast.makeText(this@ProfileActivity, "응답을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        when (updateMessage) {
                            "1" -> {
                                Toast.makeText(this@ProfileActivity, "프로필 수정 성공", Toast.LENGTH_SHORT).show()
                                navigateToMainActivity()
                            }
                            "0" -> {
                                Toast.makeText(this@ProfileActivity, "닉네임이 이미 존재합니다. 다른 닉네임을 선택하세요.", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this@ProfileActivity, "알 수 없는 응답: $updateMessage", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "프로필 수정 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("ProfileActivity", "Network error: ${t.message}", t)
                Toast.makeText(this@ProfileActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uploadProfileImage(imageUri: Uri, username: String, token: String) {
        val file = File(getRealPathFromURI(imageUri)!!)
        val requestFile = RequestBody.create(contentResolver.getType(imageUri)?.toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val authHeader = "Bearer $token"

        RetrofitClient.userApi.uploadProfileImage(username, authHeader, body).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "프로필 이미지 업로드 성공", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileActivity, "프로필 이미지 업로드 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("ProfileActivity", "Network error: ${t.message}", t)
                Toast.makeText(this@ProfileActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        var filePath: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            filePath = cursor.getString(idx)
            cursor.close()
        }
        return filePath
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@ProfileActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}