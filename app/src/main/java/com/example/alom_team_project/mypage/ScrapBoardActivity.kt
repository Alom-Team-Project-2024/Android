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
import com.example.alom_team_project.databinding.ActivityScrapBoardBinding
import com.example.alom_team_project.question_board.QuestionAdapterClass
import com.example.alom_team_project.question_board.QuestionClass
import com.example.alom_team_project.login.UserApi
import com.example.alom_team_project.question_board.QuestionPostFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScrapBoardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScrapBoardBinding
    private lateinit var adapter: QuestionAdapterClass
    private lateinit var scrapQuestionList: ArrayList<QuestionClass>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScrapBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        // scrapQuestionList 초기화
        scrapQuestionList = arrayListOf()

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
        adapter = QuestionAdapterClass(scrapQuestionList)
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

        // 사용자 이름을 가져오는 메소드 추가 (예시: 현재 로그인된 사용자의 사용자 이름을 가져오는 메소드)
        val username = getUsername() ?: ""

        api.getScrapInfo(username, "Bearer $token").enqueue(object : Callback<QuestionClass> {
            override fun onResponse(call: Call<QuestionClass>, response: Response<QuestionClass>) {
                if (response.isSuccessful) {
                    Log.d("FETCH_DATA", "Data fetched successfully")

                    response.body()?.let { post ->
                        scrapQuestionList.clear()
                        scrapQuestionList.add(post) // 리스트에 단일 항목을 추가
                        adapter.filter(binding.etSearch.text.toString())  // 현재 검색어로 필터링
                    }
                } else {
                    Log.e("FETCH_DATA", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@ScrapBoardActivity, "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<QuestionClass>, t: Throwable) {
                Log.e("FETCH_DATA", "Failed to fetch data", t)
                Toast.makeText(this@ScrapBoardActivity, "Failed to fetch data: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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

    private fun getUsername(): String? {
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        return sharedPref.getString("username", null)
    }

    private fun openPostQuestionFragment() {
        val fragment = QuestionPostFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.questionViewPage, fragment)
            .addToBackStack(null)
            .commit()
    }
}
