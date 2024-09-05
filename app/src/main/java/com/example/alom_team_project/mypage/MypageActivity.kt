package com.example.alom_team_project.mypage

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.job_board.MentorAdapterClass
import com.example.alom_team_project.job_board.MentorClass
import com.example.alom_team_project.job_board.MentorDetailFragment
import com.example.alom_team_project.job_board.MentorPostFragment
import com.example.alom_team_project.login.LoginActivity
import com.example.alom_team_project.login.UserApi
import com.example.alom_team_project.question_board.AnswerFragment
import com.example.alom_team_project.question_board.QuestionAdapterClass
import com.example.alom_team_project.question_board.QuestionClass
import com.example.alom_team_project.question_board.QuestionPostFragment
import com.google.android.material.internal.ViewUtils.hideKeyboard
import okhttp3.ResponseBody
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
    private lateinit var ivProfileImage: ImageView
    private lateinit var tvProfileEdit: TextView
    private lateinit var recyclerViewQuestion: RecyclerView
    private lateinit var recyclerViewMentor: RecyclerView
    private lateinit var questionAdapter: QuestionAdapterClass
    private lateinit var mentorAdapter: MentorAdapterClass
    private val scrapQuestionList = arrayListOf<QuestionClass>() // 질문 데이터 리스트
    private val scrapMentorList = arrayListOf<MentorClass>() // 멘토 데이터 리스트

    private val editProfileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // 정보 수정 후 돌아올 때 사용자 정보 새로고침
            getUserProfile()
        }
    }

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
        ivProfileImage = findViewById(R.id.iv_profileImage)
        tvProfileEdit = findViewById(R.id.tv_profile_edit)
        recyclerViewQuestion = findViewById(R.id.question_board_item)
        recyclerViewMentor = findViewById(R.id.mentor_board_item)

        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        tvProfileEdit.setOnClickListener {
            val intent = Intent(this, ProfileEditActivity::class.java)
            editProfileLauncher.launch(intent)
        }

        // 버튼 클릭 시 MyPostActivity로 이동
        btnMyPosts.setOnClickListener {
            val intent = Intent(this, MyPostsQuestionActivity::class.java)
            startActivity(intent)
        }

        // 텍스트뷰 클릭 시 ScrapBoardActivity로 이동
        tvMoreScrap.setOnClickListener {
            val intent = Intent(this, ScrapQuestionBoardActivity::class.java)
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

        // 스크랩 데이터 가져오기
        fetchScrapData()
        fetchMentorData()
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

                        // 프로필 이미지 가져오기
                        getProfileImage(username, token)
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

    private fun getProfileImage(username: String, token: String) {
        val api = RetrofitClient.userApi
        api.getProfileImage(username, "Bearer $token").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.byteStream()?.let { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        ivProfileImage.setImageBitmap(bitmap)
                    }
                } else {
                    Log.e("ProfileImage", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@MypageActivity, "Failed to fetch profile image", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("ProfileImage", "Failed to fetch profile image", t)
                Toast.makeText(this@MypageActivity, "Failed to fetch profile image: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchScrapData() {
        val token = getJwtToken()
        Log.d("FETCH_DATA", "Fetching data with token: $token")

        val api = RetrofitClient.instance.create(UserApi::class.java)
        val username = getUsername() ?: ""

        api.getScrapQuestionInfo(username, "Bearer $token").enqueue(object : Callback<List<QuestionClass>> {
            override fun onResponse(call: Call<List<QuestionClass>>, response: Response<List<QuestionClass>>) {
                if (response.isSuccessful) {
                    Log.d("FETCH_DATA", "Data fetched successfully")

                    response.body()?.let { posts ->
                        if (posts.isNotEmpty()) {
                            // 최신 질문 하나만 추출
                            val latestQuestion = posts[0]
                            scrapQuestionList.clear()
                            scrapQuestionList.add(latestQuestion)
                            setupQuestionRecyclerView()
                        }
                    }
                } else {
                    Log.e("FETCH_DATA", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@MypageActivity, "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<QuestionClass>>, t: Throwable) {
                Log.e("FETCH_DATA", "Failed to fetch data", t)
                Toast.makeText(this@MypageActivity, "Failed to fetch data: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchMentorData() {
        val token = getJwtToken()
        Log.d("FETCH_DATA", "Fetching data with token: $token")

        val api = RetrofitClient.instance.create(UserApi::class.java)
        val username = getUsername() ?: ""
        api.getScrapMentorInfo(username, "Bearer $token").enqueue(object : Callback<List<MentorClass>> {
            override fun onResponse(call: Call<List<MentorClass>>, response: Response<List<MentorClass>>) {
                if (response.isSuccessful) {
                    Log.d("FETCH_DATA", "Data fetched successfully")
                    response.body()?.let { mentors ->
                        if (mentors.isNotEmpty()) {
                            // 최신 멘토 하나만 추출
                            val latestMentor = mentors[0]
                            scrapMentorList.clear()
                            scrapMentorList.add(latestMentor)
                            setupMentorRecyclerView()
                        }
                    }
                } else {
                    Log.e("FETCH_DATA", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@MypageActivity, "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<MentorClass>>, t: Throwable) {
                Log.e("FETCH_DATA", "Failed to fetch data", t)
                Toast.makeText(this@MypageActivity, "Failed to fetch data: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupQuestionRecyclerView() {
        questionAdapter = QuestionAdapterClass(
            scrapQuestionList,
            onItemClickListener = { questionId ->
                Log.d("MypageActivity", "Question clicked with ID: $questionId")
                // AnswerFragment로 이동
                val fragment = AnswerFragment().apply {
                    arguments = Bundle().apply {
                        putLong("QUESTION_ID", questionId)
                        Log.d("QuestionRecyclerView", "Question clicked: ID = $questionId")

                    }
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mypageViewPage, fragment)  // Fragment를 담을 컨테이너 ID 수정
                    .addToBackStack(null)
                    .commit()
            }
        )
        recyclerViewQuestion.layoutManager = LinearLayoutManager(this)
        recyclerViewQuestion.adapter = questionAdapter

    }

    // 화면 터치 시 키보드 내리기
    private fun hideKeyboard() {
        if (this != null && this.currentFocus != null) {
            val inputManager: InputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    private fun setupMentorRecyclerView() {
        mentorAdapter = MentorAdapterClass(
            scrapMentorList,
            onItemClickListener = { mentorId ->
                Log.d("MypageActivity", "Mentor clicked with ID: $mentorId")

                // MentorDetailFragment로 이동
                val fragment = MentorDetailFragment().apply {
                    arguments = Bundle().apply {
                        putLong("MENTOR_ID", mentorId)
                    }
                }

                // Fragment 트랜잭션을 통해 화면 전환
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mypageViewPage, fragment)  // fragment_container로 ID 수정
                    .addToBackStack(null)
                    .commit()

                Log.d("FragmentTransaction", "Fragment replaced in container with ID: $mentorId")
            }
        )
        recyclerViewMentor.layoutManager = LinearLayoutManager(this)
        recyclerViewMentor.adapter = mentorAdapter
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
