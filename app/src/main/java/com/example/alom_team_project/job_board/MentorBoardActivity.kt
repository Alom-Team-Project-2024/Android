package com.example.alom_team_project.job_board

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.databinding.ActivityMentorBoardBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MentorBoardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMentorBoardBinding
    private lateinit var adapter: MentorAdapterClass
    private lateinit var mentorList: ArrayList<MentorClass>
    private lateinit var mentorService: MentorPostService
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private val refreshInterval: Long = 12000 // 1분

    private var status = "mentor"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMentorBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener{
            finish()
        }

        // mentor 초기화
        mentorList = arrayListOf()

        // mentorService 초기화
        mentorService = RetrofitClient.instance.create(MentorPostService::class.java)

        // RecyclerView 어댑터 및 레이아웃 매니저 설정
        setupRecyclerView()


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
        binding.btnMentor.setOnClickListener {
            selectButton(binding.btnMentor)
            status = "mentor"
            fetchData(status)
        }

        binding.btnMentee.setOnClickListener {
            selectButton(binding.btnMentee)
            status = "mentee"
            fetchData(status)
        }

        // 초기 선택 상태 설정 (기본적으로 질문 게시판 선택됨)
        selectButton(binding.btnMentor)

        binding.writingButton.setOnClickListener {
            openPostMentorFragment()
        }

        setupAutoRefresh()
    }

    private fun setupRecyclerView() {
            adapter = MentorAdapterClass(
                mentorList = mentorList,
                onItemClickListener = { mentorId ->
                    // MentorDetailFragment로 이동
                    val fragment = MentorDetailFragment().apply {
                        arguments = Bundle().apply {
                            putLong("MENTOR_ID", mentorId)
                        }
                    }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mentorViewPage, fragment)  // Fragment를 담을 컨테이너 ID 수정
                        .addToBackStack(null)
                        .commit()
                }
            )

            binding.MentorRecyclerView.adapter = adapter
            binding.MentorRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun getJwtToken(): String {
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        return sharedPref.getString("jwt_token", "") ?: ""
    }


    private fun fetchData(status:String) {
        var category: String

        if(status=="mentor"){
            category = "FIND_MENTOR"
        }
        else{
            category = "FIND_MENTEE"
        }

        val token = getJwtToken()
        //Log.d("FETCH_DATA", "Fetching data with token: $token")

        mentorService.getMentors("Bearer $token",category).enqueue(object :
            Callback<List<MentorClass>> {
            override fun onResponse(call: Call<List<MentorClass>>, response: Response<List<MentorClass>>) {
                if (response.isSuccessful) {
                    //Log.d("FETCH_DATA", "Data fetched successfully")

                    response.body()?.let { mentors ->
                        mentorList.clear()
                        mentorList.addAll(mentors)
                        adapter.filter(binding.etSearch.text.toString())  // 현재 검색어로 필터링
                    }
                } else {
                    Log.e("FETCH_DATA", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@MentorBoardActivity, "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<MentorClass>>, t: Throwable) {
                Log.e("FETCH_DATA", "Failed to fetch data", t)
                Toast.makeText(this@MentorBoardActivity, "Failed to fetch data: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun selectButton(selectedButton: TextView) {
        // 버튼 색상 설정
        val selectedColor = "#1391B4" // 파란색
        val unselectedColor = "#000000" // 기본 검은색

        // 언더바 visibility 조정
        if (selectedButton.id == binding.btnMentor.id) {
            binding.btnMentor.setTextColor(Color.parseColor(selectedColor))
            binding.btnMentee.setTextColor(Color.parseColor(unselectedColor))
            binding.underbar.visibility = android.view.View.VISIBLE
            binding.underbar2.visibility = android.view.View.INVISIBLE
        } else {
            binding.btnMentor.setTextColor(Color.parseColor(unselectedColor))
            binding.btnMentee.setTextColor(Color.parseColor(selectedColor))
            binding.underbar.visibility = android.view.View.INVISIBLE
            binding.underbar2.visibility = android.view.View.VISIBLE
        }
    }

    private fun setupAutoRefresh() {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                fetchData(status)
                handler.postDelayed(this, refreshInterval)
            }
        }
        handler.post(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable) // Activity가 파괴될 때 Runnable 제거
    }



    private fun openPostMentorFragment() {
        val fragment = MentorPostFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mentorViewPage, fragment)
            .addToBackStack(null)
            .commit()
    }

}

