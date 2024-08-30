package com.example.alom_team_project.mypage

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.databinding.ActivityMyPostsBinding
import com.example.alom_team_project.question_board.QuestionAdapterClass
import com.example.alom_team_project.job_board.MentorClass
import com.example.alom_team_project.login.UserApi
import com.example.alom_team_project.question_board.QuestionPostFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPostsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyPostsBinding
    private lateinit var adapter: QuestionAdapterClass
    private lateinit var myPostList: ArrayList<MentorClass>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        // myPostList 초기화
        myPostList = arrayListOf()

        // RecyclerView 어댑터 및 레이아웃 매니저 설정
        setupRecyclerView()

        // 데이터 가져오기
        fetchData()

        // 검색 텍스트 변화 감지
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 텍스트 변화 전의 행동을 정의
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트가 변경될 때마다 어댑터의 필터링 메소드 호출
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // 텍스트 변화 후의 행동을 정의
            }
        })

        // 버튼 클릭 시 언더바 전환 및 색상 변경
        binding.btnQuestion.setOnClickListener {
            selectButton(binding.btnQuestion)
        }

        binding.btnMentor.setOnClickListener {
            selectButton(binding.btnMentor)
        }

        // 초기 선택 상태 설정 (기본적으로 질문 게시판 선택됨)
        selectButton(binding.btnQuestion)
    }

    private fun setupRecyclerView() {
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //adapter = QuestionAdapterClass(myPostList)
        binding.QuestionRecyclerView.adapter = adapter
        binding.QuestionRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun getJwtToken(): String {
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        return sharedPref.getString("jwt_token", "") ?: ""
    }

    private fun fetchData() {
        val token = getJwtToken()
        Log.d("FETCH_DATA", "Fetching data with token: $token")

        // RetrofitClient의 UserApi 인스턴스를 생성합니다
        val api = RetrofitClient.instance.create(UserApi::class.java)

        // 사용자 이름(닉네임)을 가져오는 메소드 추가 (현재 로그인된 사용자의 닉네임을 가져오는 메소드)
        val username = getUsername() ?: ""

        // 사용자 프로필 가져오기
        api.getUserProfile(username, "Bearer $token").enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        // UI에 데이터 반영
                        val nickname = user.nickname ?: ""
                        fetchQuestions(nickname, token)
                    }
                } else {
                    Log.e("FETCH_DATA", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@MyPostsActivity, "Failed to fetch user profile: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("FETCH_DATA", "Failed to fetch user profile", t)
                Toast.makeText(this@MyPostsActivity, "Failed to fetch user profile: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchQuestions(nickname: String, token: String) {
        val api = RetrofitClient.instance.create(UserApi::class.java)

        api.getQuestionsByNickname(nickname, "Bearer $token").enqueue(object : Callback<List<MentorClass>> {
            override fun onResponse(call: Call<List<MentorClass>>, response: Response<List<MentorClass>>) {
                if (response.isSuccessful) {
                    Log.d("FETCH_DATA", "Data fetched successfully")

                    response.body()?.let { posts ->
                        myPostList.clear()
                        myPostList.addAll(posts) // 리스트에 항목들을 추가
                        adapter.filter(binding.etSearch.text.toString())  // 현재 검색어로 필터링
                    }
                } else {
                    Log.e("FETCH_DATA", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@MyPostsActivity, "Failed to fetch questions: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<MentorClass>>, t: Throwable) {
                Log.e("FETCH_DATA", "Failed to fetch questions", t)
                Toast.makeText(this@MyPostsActivity, "Failed to fetch questions: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUsername(): String? {
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        return sharedPref.getString("username", null)
    }

    private fun selectButton(selectedButton: TextView) {
        // 버튼 색상 설정
        val selectedQuestionColor = "#4825D8" // 보라색
        val selectedMentorColor = "#1391B4" // 파란색
        val unselectedColor = "#000000" // 기본 검은색

        // 언더바 visibility 조정
        if (selectedButton.id == binding.btnQuestion.id) {
            binding.btnQuestion.setTextColor(Color.parseColor(selectedQuestionColor))
            binding.btnMentor.setTextColor(Color.parseColor(unselectedColor))
            binding.underbar.visibility = android.view.View.VISIBLE
            binding.underbar2.visibility = android.view.View.INVISIBLE
        } else {
            binding.btnQuestion.setTextColor(Color.parseColor(unselectedColor))
            binding.btnMentor.setTextColor(Color.parseColor(selectedMentorColor))
            binding.underbar.visibility = android.view.View.INVISIBLE
            binding.underbar2.visibility = android.view.View.VISIBLE
        }
    }

    private fun openPostQuestionFragment() {
        val fragment = QuestionPostFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.questionViewPage, fragment)
            .addToBackStack(null)
            .commit()
    }
}
