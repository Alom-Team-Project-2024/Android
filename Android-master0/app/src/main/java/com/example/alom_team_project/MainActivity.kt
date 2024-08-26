package com.example.alom_team_project

import QuestionPostResponse
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alom_team_project.databinding.ActivityMainBinding
import com.example.alom_team_project.home.HomeRecordAdapter
import com.example.alom_team_project.home.HomeRecordData
import com.example.alom_team_project.home.NavigationFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var recordList: ArrayList<HomeRecordData>
    private lateinit var recordAdapter: HomeRecordAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 네비게이션 프래그먼트
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.navfragmentContainer, NavigationFragment())
        transaction.commit()

        recordList = ArrayList()
        recordAdapter = HomeRecordAdapter(recordList)
        binding.rvRecord.adapter = recordAdapter
        binding.rvRecord.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        fetchQuestions()
    }

    private fun fetchQuestions() {
        val token = getJwtToken()
        if (token != null) {
            RetrofitClient.userApi.getQuestions("Bearer $token").enqueue(object : Callback<List<QuestionPostResponse>> {
                override fun onResponse(call: Call<List<QuestionPostResponse>>, response: Response<List<QuestionPostResponse>>) {
                    if (response.isSuccessful) {
                        response.body()?.let { questions ->
                            //updateRecordList(questions)
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Failed to load questions", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<QuestionPostResponse>>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateRecordList(questions: List<QuestionPostResponse>) {
        recordList.clear()
        questions.forEach { question ->
            val replies = question.replies ?: emptyList() // replies가 null일 경우 빈 리스트로 처리
            val images = question.images ?: emptyList()   // images가 null일 경우 빈 리스트로 처리

            val mentorName = if (replies.isNotEmpty()) replies[0].title else "No replies"
            val answerText = if (replies.isNotEmpty()) replies[0].text else "No answer"
            val imageUrl = if (images.isNotEmpty()) images[0].imageUrl else ""

            recordList.add(
                HomeRecordData(
                    title = question.subject,
                    status = "진행중",  // 상태를 기본적으로 '진행중'으로 설정
                    imageUrl = imageUrl,
                    mentorName = mentorName,
                    answer = answerText
                )
            )
        }
        recordAdapter.notifyDataSetChanged()
    }


    private fun getJwtToken(): String? {
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }
}

//.