package com.example.alom_team_project.mypage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AppCompatActivity
import com.example.alom_team_project.MainActivity
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import android.provider.MediaStore
import androidx.activity.result.PickVisualMediaRequest
import okhttp3.ResponseBody

class ProfileActivity : AppCompatActivity() {

    private lateinit var etNickname: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnCheckDouble: Button
    private lateinit var tvCheckDouble: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var btnProfile: ImageButton
    private lateinit var btnBack:ImageButton

    private var selectedImageUri: Uri? = null
    private var isNicknameAvailable = false

    // Register for ActivityResult to pick a single image or video
    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            selectedImageUri = uri
            imgProfile.setImageURI(uri)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        etNickname = findViewById(R.id.et_nickname)
        btnSubmit = findViewById(R.id.btn_submit)
        btnCheckDouble = findViewById(R.id.btn_check_double)
        tvCheckDouble = findViewById(R.id.tv_check_double)
        imgProfile = findViewById(R.id.img_profile)
        btnProfile = findViewById(R.id.btn_profile)
        btnBack = findViewById(R.id.btn_back)

        imgProfile.setBackgroundResource(R.drawable.group_256)

        btnProfile.setOnClickListener {
            // Launch the photo picker to select an image or video
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnCheckDouble.setOnClickListener {
            val nickname = etNickname.text.toString().trim()
            val token = getJwtToken()

            if (nickname.isNotEmpty() && token != null) {
                checkNicknameDuplication(nickname, token)
            } else {
                Toast.makeText(this, "닉네임 또는 토큰을 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        btnSubmit.setOnClickListener {
            val nickname = etNickname.text.toString().trim()
            val token = getJwtToken()
            val username = getUsername()

            if (nickname.isNotEmpty() && token != null && username != null) {
                if (isNicknameAvailable) {
                    updateProfile(nickname, username, token)
                    if (selectedImageUri != null) {
                        uploadProfileImage(selectedImageUri!!, username, token)
                    }
                } else {
                    Toast.makeText(this, "닉네임 중복을 확인해주세요.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "닉네임, 학번 또는 토큰을 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
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

    private fun checkNicknameDuplication(nickname: String, token: String) {
        val authHeader = "Bearer $token"
        RetrofitClient.userApi.checkDuplicateUser(nickname, authHeader)
            .enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful) {
                        val isAvailable = response.body() ?: false
                        if (isAvailable) {
                            tvCheckDouble.text = "사용 가능한 닉네임입니다."
                            tvCheckDouble.setTextColor(android.graphics.Color.parseColor("#006917"))
                            Toast.makeText(
                                this@ProfileActivity,
                                "사용 가능한 닉네임입니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            isNicknameAvailable = true
                        } else {
                            tvCheckDouble.text = "사용 불가능한 닉네임입니다."
                            tvCheckDouble.setTextColor(android.graphics.Color.parseColor("#FF0000"))
                            Toast.makeText(
                                this@ProfileActivity,
                                "사용 불가능한 닉네임입니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            isNicknameAvailable = false
                        }
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            "닉네임 중복 체크에 실패했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    Log.e("ProfileActivity", "Network error: ${t.message}", t)
                    Toast.makeText(
                        this@ProfileActivity,
                        "네트워크 오류: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updateProfile(nickname: String, username: String, token: String) {
        val profileData = mapOf("username" to username, "nickname" to nickname)
        val authHeader = "Bearer $token"

        RetrofitClient.userApi.updateProfile(authHeader, profileData)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val updateMessage = response.body()
                        if (updateMessage == null) {
                            Toast.makeText(
                                this@ProfileActivity,
                                "응답을 불러올 수 없습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            when (updateMessage) {
                                "1" -> {
                                    Toast.makeText(
                                        this@ProfileActivity,
                                        "프로필 수정 성공",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navigateToMainActivity()
                                }

                                "0" -> {
                                    Toast.makeText(
                                        this@ProfileActivity,
                                        "닉네임이 이미 존재합니다. 다른 닉네임을 선택하세요.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                else -> {
                                    Toast.makeText(
                                        this@ProfileActivity,
                                        "알 수 없는 응답: $updateMessage",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            "프로필 수정 실패: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("ProfileActivity", "Network error: ${t.message}", t)
                    Toast.makeText(
                        this@ProfileActivity,
                        "네트워크 오류: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun uploadProfileImage(imageUri: Uri, username: String, token: String) {
        val authHeader = "Bearer $token"
        val file = File(getRealPathFromURI(imageUri) ?: return)

        val requestFile = file.asRequestBody("image/svg+xml".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        RetrofitClient.userApi.uploadProfileImage(username, authHeader, body)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string()  // Read response as a string
                        Log.d("ProfileActivity", "Response: $responseBody")
                        Toast.makeText(this@ProfileActivity, "프로필 이미지 업로드 성공", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorBody = response.errorBody()?.string()  // Read error body
                        Log.e("ProfileActivity", "응답 오류: $errorBody")
                        Toast.makeText(this@ProfileActivity, "프로필 이미지 업로드 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("ProfileActivity", "네트워크 오류: ${t.message}", t)
                    Toast.makeText(this@ProfileActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun getRealPathFromURI(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex)
            }
        }
        return null
    }


    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
