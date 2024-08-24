/*
package com.example.alom_team_project

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alom_team_project.databinding.ActivityScrapBoardBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScrapBoardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScrapBoardBinding
    private lateinit var adapter: QuestionAdapterClass
    private lateinit var scrapQuestionList: ArrayList<QuestionClass>
    private val questionService: UserApi by lazy { RetrofitClient.userApi } // RetrofitClient 사용

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScrapBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 스크랩된 질문 리스트 초기화
        scrapQuestionList = arrayListOf()

        // RecyclerView 어댑터 및 레이아웃 매니저 설정
        setupRecyclerView()

        // 스크랩된 질문 데이터 가져오기
        fetchScrapData()

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        // 검색 텍스트 변화 감지
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트가 변경될 때마다 어댑터의 필터링 메소드 호출
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = QuestionAdapterClass(scrapQuestionList)
        binding.QuestionRecyclerView.adapter = adapter
        binding.QuestionRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchScrapData() {
        val token = getJwtToken()
        val username = getUsername()

        Log.d("FETCH_SCRAP_DATA", "Fetching scrap data with token: $token")

        // 스크랩된 질문들을 가져오는 API 호출
        questionService.getScrapQuestions("Bearer $token").enqueue(object : Callback<List<QuestionClass>> {
            override fun onResponse(call: Call<List<QuestionClass>>, response: Response<List<QuestionClass>>) {
                if (response.isSuccessful) {
                    Log.d("FETCH_SCRAP_DATA", "Scrap data fetched successfully")
                    response.body()?.let { questions ->
                        scrapQuestionList.clear()
                        scrapQuestionList.addAll(questions)
                        adapter.filter(binding.etSearch.text.toString())  // 현재 검색어로 필터링
                    }
                } else {
                    Log.e("FETCH_SCRAP_DATA", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@ScrapBoardActivity, "Failed to fetch scrap data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<QuestionClass>>, t: Throwable) {
                Log.e("FETCH_SCRAP_DATA", "Failed to fetch scrap data", t)
                Toast.makeText(this@ScrapBoardActivity, "Failed to fetch scrap data: ${t.message}", Toast.LENGTH_SHORT).show()
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
*/
