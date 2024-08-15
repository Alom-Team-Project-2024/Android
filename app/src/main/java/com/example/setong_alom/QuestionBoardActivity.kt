package com.example.setong_alom

import QuestionPostService
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.setong_alom.databinding.ActivityQuestionBoardBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QuestionBoardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuestionBoardBinding
    private lateinit var adapter: QuestionAdapterClass
    private lateinit var questionList: ArrayList<QuestionClass>
    private lateinit var questionService: QuestionPostService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // questionList 초기화
        questionList = arrayListOf()

        // questionService 초기화
        questionService = RetrofitClient.instance.create(QuestionPostService::class.java)

        // RecyclerView 어댑터 및 레이아웃 매니저 설정
        setupRecyclerView()

        // 데이터 가져오기
        fetchData()

        binding.writingButton.setOnClickListener {
            openPostQuestionFragment()
        }

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
    }

    private fun setupRecyclerView() {
        adapter = QuestionAdapterClass(questionList)
        binding.QuestionRecyclerView.adapter = adapter
        binding.QuestionRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchData() {
        val token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6IjIyMDExMzc1Iiwicm9sZSI6IlVTRVIiLCJuaWNrbmFtZSI6InVzZXI3MDIiLCJpYXQiOjE3MjM3MTU4MDUsImV4cCI6MTcyMzcxNzYwNX0.42NmgXInVMSz_oBNk9AdmRghafPvgk61o74BouI04vM"
        Log.d("FETCH_DATA", "Fetching data with token: $token")

        questionService.getQuestions(token).enqueue(object : Callback<List<QuestionClass>> {
            override fun onResponse(call: Call<List<QuestionClass>>, response: Response<List<QuestionClass>>) {
                if (response.isSuccessful) {
                    Log.d("FETCH_DATA", "Data fetched successfully")
                    response.body()?.let { questions ->
                        questionList.clear()
                        questionList.addAll(questions)
                        adapter.filter(binding.etSearch.text.toString())  // 현재 검색어로 필터링
                    }
                } else {
                    Log.e("FETCH_DATA", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@QuestionBoardActivity, "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<QuestionClass>>, t: Throwable) {
                Log.e("FETCH_DATA", "Failed to fetch data", t)
                Toast.makeText(this@QuestionBoardActivity, "Failed to fetch data: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun openPostQuestionFragment() {
        val fragment = QuestionPostFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.questionViewPage, fragment)
            .addToBackStack(null)
            .commit()
    }
}
