package com.example.alom_team_project.mypage

import android.content.Intent
import android.graphics.BitmapFactory
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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import okhttp3.ResponseBody

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var etNickname: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnCheckDouble: Button
    private lateinit var tvCheckDouble: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var btnProfile: ImageButton
    private lateinit var btnBack: ImageButton

    private var selectedImageUri: Uri? = null
    private var isNicknameAvailable = false
    private var originalNickname: String? = null
    private var isNicknameChecked = false // Track if the nickname has been checked
    private fun finishWithResult() {
        setResult(RESULT_OK)
        finish()
    }

    // 이미지 선택 및 바로 표시
    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            selectedImageUri = uri
            // 선택한 이미지를 Glide로 로드하고 원형 크롭 후 표시
            Glide.with(this)
                .load(uri)
                .apply(RequestOptions.circleCropTransform()) // 원형 크롭 적용
                .into(imgProfile) // imgProfile에 이미지 표시
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        etNickname = findViewById(R.id.et_nickname)
        btnSubmit = findViewById(R.id.btn_submit)
        btnCheckDouble = findViewById(R.id.btn_check_double)
        tvCheckDouble = findViewById(R.id.tv_check_double)
        imgProfile = findViewById(R.id.img_profile)
        btnProfile = findViewById(R.id.btn_profile)
        btnBack = findViewById(R.id.btn_back)

        // Load user profile on create
        getUserProfile()

        btnProfile.setOnClickListener {
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
            val newNickname = etNickname.text.toString().trim()
            val token = getJwtToken()
            val username = getUsername()

            Log.d("ProfileEditActivity", "New nickname: $newNickname, Token: $token, Username: $username")

            if (newNickname.isNotEmpty() && token != null && username != null) {
                if (newNickname == originalNickname && selectedImageUri != null) {
                    // 이미지 변경만 있을 때 서버로 요청
                    Log.d("ProfileEditActivity", "Only profile image changed, uploading image...")
                    uploadProfileImage(selectedImageUri!!, username, token)
                    Log.d("ProfileEditActivity", "${selectedImageUri}")

                } else {
                    if (isNicknameChecked && isNicknameAvailable) {
                        Log.d("ProfileEditActivity", "Profile update and image upload started...")
                        updateProfile(newNickname, username, token)
                        if (selectedImageUri != null) {
                            uploadProfileImage(selectedImageUri!!, username, token)
                        }
                    } else {
                        Log.d("ProfileEditActivity", "Nickname check needed or nickname duplication found")
                        Toast.makeText(this, "닉네임 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.d("ProfileEditActivity", "Nickname, token or username missing")
                Toast.makeText(this, "닉네임, 학번 또는 토큰을 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getUserProfile() {
        val api = RetrofitClient.userApi
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
                        originalNickname = user.nickname
                        etNickname.setText(originalNickname)

                        Log.d("ProfileEditActivity", "${user.profileImage}")

                        if (user.profileImage != null) {
                            val imageUrl = getAbsoluteUrl(user.profileImage)
                            Log.d("ProfileEditActivity", "$imageUrl")

                            Glide.with(this@ProfileEditActivity)
                                .load(imageUrl)
                                .apply(RequestOptions.circleCropTransform()) // 원형 변환
                                .into(imgProfile)
                        } else {
                            Glide.with(this@ProfileEditActivity)
                                .load(R.drawable.group_282)
                                .apply(RequestOptions.circleCropTransform()) // 원형 변환
                                .into(imgProfile)
                        }


                    }
                } else {
                    Toast.makeText(this@ProfileEditActivity, "사용자 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("UserProfile", "Network Error: ${t.message}")
                Toast.makeText(this@ProfileEditActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getProfileImage(username: String, token: String) {
        val api = RetrofitClient.userApi

        api.getProfileImage(username, "Bearer $token").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val imageUrl = response.body()
                    Glide.with(this@ProfileEditActivity)
                        .load(imageUrl)
                        .apply(RequestOptions.circleCropTransform()) // 원형 크롭 적용
                        .into(imgProfile)

                    Log.d("ProfileEditActivity", "Profile image loaded and cropped successfully")
                } else {
                    Log.e("ProfileImage", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@ProfileEditActivity, "프로필 이미지를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("ProfileImage", "Failed to fetch profile image", t)
                Toast.makeText(this@ProfileEditActivity, "프로필 이미지 가져오기 실패: ${t.message}", Toast.LENGTH_SHORT).show()
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
                                this@ProfileEditActivity,
                                "사용 가능한 닉네임입니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            isNicknameAvailable = true
                            isNicknameChecked = true // Mark as checked
                        } else {
                            tvCheckDouble.text = "사용 불가능한 닉네임입니다."
                            tvCheckDouble.setTextColor(android.graphics.Color.parseColor("#FF0000"))
                            Toast.makeText(
                                this@ProfileEditActivity,
                                "사용 불가능한 닉네임입니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            isNicknameAvailable = false
                            isNicknameChecked = true // Mark as checked
                        }
                    } else {
                        Toast.makeText(
                            this@ProfileEditActivity,
                            "닉네임 중복 체크에 실패했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    Log.e("ProfileEditActivity", "Network error: ${t.message}", t)
                    Toast.makeText(
                        this@ProfileEditActivity,
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
                        if (updateMessage == "1") {
                            Toast.makeText(
                                this@ProfileEditActivity,
                                "프로필 수정 성공",
                                Toast.LENGTH_SHORT
                            ).show()
                            // 프로필 업데이트 후 최신 프로필 다시 불러오기
                            getUserProfile()
                        } else {
                            Toast.makeText(
                                this@ProfileEditActivity,
                                "닉네임이 이미 존재합니다. 다른 닉네임을 선택하세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@ProfileEditActivity,
                            "프로필 수정 실패: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("ProfileEditActivity", "Network error: ${t.message}", t)
                    Toast.makeText(
                        this@ProfileEditActivity,
                        "네트워크 오류: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun uploadProfileImage(imageUri: Uri, username: String, token: String) {
        val authHeader = "Bearer $token"
        val filePath = getRealPathFromURI(imageUri)
        val file = File(filePath ?: return)

        Log.d("ProfileEditActivity", "Uploading file: $filePath")

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        RetrofitClient.userApi.uploadProfileImage(username, authHeader, body)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Log.d("ProfileEditActivity", "Response code: ${response.code()}")
                        Log.d("ProfileEditActivity", "Response body: ${response.body()?.string()}")
                        Toast.makeText(this@ProfileEditActivity, "프로필 이미지 업로드 성공", Toast.LENGTH_SHORT).show()
                        // 이미지 업로드 성공 후 최신 프로필 다시 불러오기
                        getUserProfile()
                    } else {
                        Log.e("ProfileEditActivity", "Upload failed with code: ${response.code()}")
                        Log.e("ProfileEditActivity", "Upload failed with message: ${response.message()}")
                        Toast.makeText(this@ProfileEditActivity, "프로필 이미지 업로드 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("ProfileEditActivity", "Network error: ${t.message}", t)
                    Toast.makeText(this@ProfileEditActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
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

    fun getAbsoluteUrl(relativeUrl: String): String {
        val baseUrl = "http://15.165.213.186/" // 서버의 기본 URL
        return baseUrl + relativeUrl
    }
}
