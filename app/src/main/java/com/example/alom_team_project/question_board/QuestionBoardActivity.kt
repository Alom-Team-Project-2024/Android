package com.example.alom_team_project.question_board



import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.databinding.ActivityQuestionBoardBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import setupGlide

class QuestionBoardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuestionBoardBinding
    private lateinit var adapter: QuestionAdapterClass
    private lateinit var questionList: ArrayList<QuestionClass>
    private lateinit var questionService: QuestionPostService
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private val refreshInterval: Long = 12000 // 12초

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGlide(this)

        binding.backButton.setOnClickListener{
            finish()
        }

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

        setupAutoRefresh()

        // 화면 클릭 시 키보드 내리기
        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
            }
            true
        }
    }

    private fun setupRecyclerView() {
        adapter = QuestionAdapterClass(
            questionList = questionList,
            onItemClickListener = { questionId ->

                binding.etSearch.setText("")

                // AnswerFragment로 이동
                val fragment = AnswerFragment().apply {
                    arguments = Bundle().apply {
                        putLong("QUESTION_ID", questionId)
                    }
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.questionViewPage, fragment)  // Fragment를 담을 컨테이너 ID 수정
                    .addToBackStack(null)
                    .commit()
            }
        )

        binding.QuestionRecyclerView.adapter = adapter
        binding.QuestionRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun getJwtToken(): String {
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        return sharedPref.getString("jwt_token", "") ?: ""
    }


    private fun fetchData() {
        val token = getJwtToken()
        //Log.d("FETCH_DATA", "Fetching data with token: $token")


        questionService.getQuestions("Bearer $token").enqueue(object : Callback<List<QuestionClass>> {
            override fun onResponse(call: Call<List<QuestionClass>>, response: Response<List<QuestionClass>>) {
                if (response.isSuccessful) {
                    //Log.d("FETCH_DATA", "Data fetched successfully")

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

    private fun setupAutoRefresh() {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                fetchData()
                handler.postDelayed(this, refreshInterval)
            }
        }
        handler.post(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable) // Activity가 파괴될 때 Runnable 제거
    }

    private fun openPostQuestionFragment() {
        hideKeyboard()
        binding.etSearch.setText("")

        val fragment = QuestionPostFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.questionViewPage, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}
