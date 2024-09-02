package com.example.alom_team_project

import QuestionPostResponse
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alom_team_project.databinding.ActivityMainBinding
import com.example.alom_team_project.home.HomeRecordAdapter
import com.example.alom_team_project.home.HomeRecordData
import com.example.alom_team_project.home.NavigationFragment
import com.example.alom_team_project.login.UserApi
import com.example.alom_team_project.job_board.MentorPostResponse
import com.example.alom_team_project.mypage.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.graphics.Typeface

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
        fetchData() //홈화면 이름 불러오기
        fetchMentors()
        updateRecordCount() // 초기화 시점에서 기록 개수를 업데이트합니다.
    }

    private fun fetchQuestions() {
        val token = getJwtToken()
        if (token != null) {
            RetrofitClient.userApi.getQuestions("Bearer $token").enqueue(object : Callback<List<QuestionPostResponse>> {
                override fun onResponse(call: Call<List<QuestionPostResponse>>, response: Response<List<QuestionPostResponse>>) {
                    if (response.isSuccessful) {
                        response.body()?.let { questions ->
                            updateRecordList(questions)
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
        // 최대 항목 수를 설정합니다.
        val maxItemCount = 10

        // 새로운 데이터로 기존 데이터를 업데이트합니다.
        recordList.clear()
        questions.forEach { question ->
            val replies = question.replies ?: emptyList()
            val images = question.images ?: emptyList()
            val mentorName = if (replies.isNotEmpty()) replies[0].title else "No replies"
            val answerText = if (replies.isNotEmpty()) replies[0].text else "No answer"
            val imageUrl = if (images.isNotEmpty()) images[0].imageUrl else ""

            recordList.add(
                HomeRecordData(
                    title = question.subject,
                    commentCount = question.replyCount,
                    imageUrl = imageUrl,
                    mentorName = mentorName,
                    answer = answerText
                )
            )
        }

        // 리스트가 최대 항목 수를 초과하는 경우 초과된 항목을 제거합니다.
        if (recordList.size > maxItemCount) {
            recordList.subList(maxItemCount, recordList.size).clear()
        }

        // 어댑터에 데이터 변경 사항을 알립니다.
        recordAdapter.notifyDataSetChanged()

        // 기록 개수를 업데이트합니다.
        updateRecordCount()
    }

    private fun updateRecordCount() {
        val maxCount = 10 // 최대 표시 가능한 아이템 수
        val currentCount = recordList.size // 현재 아이템 수

        // tv_home_record_count를 "currentCount/10" 형식으로 업데이트합니다.
        binding.tvHomeRecordCount.text = "$currentCount/$maxCount"
    }


    private fun fetchMentors() {
        val token = getJwtToken() ?: return

        RetrofitClient.userApi.getMentors("Bearer $token").enqueue(object : Callback<List<MentorPostResponse>> {
            override fun onResponse(call: Call<List<MentorPostResponse>>, response: Response<List<MentorPostResponse>>) {
                if (response.isSuccessful) {
                    response.body()?.let { mentors ->
                        updateMentorViews(mentors)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load mentors", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<MentorPostResponse>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateMentorViews(mentors: List<MentorPostResponse>) {
        if (mentors.isEmpty()) return

        // 최신 글이 위로 오도록 정렬
        val sortedMentors = mentors.sortedByDescending { it.createdAt }

        // 첫 번째 아이템 업데이트
        if (sortedMentors.size > 0) {
            val mentor1 = sortedMentors[0]
            binding.tvName1.text = mentor1.writer
            binding.tvContent1.text = mentor1.text
            binding.tvTime1.text = formatTime(mentor1.createdAt)
        }

        // 두 번째 아이템 업데이트
        if (sortedMentors.size > 1) {
            val mentor2 = sortedMentors[1]
            binding.tvName2.text = mentor2.writer
            binding.tvContent2.text = mentor2.text
            binding.tvTime2.text = formatTime(mentor2.createdAt)
        }
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
                        val name = user.name ?: ""
                        binding.tvEncourage.text = "${name}님,\n" +
                                "오늘도 화이팅하세요!"
                        val encourageText = SpannableStringBuilder("${name}님,\n오늘도 화이팅하세요!")

                        encourageText.setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            name.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        binding.tvEncourage.text = encourageText
                    }
                } else {
                    Log.e("FETCH_DATA", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@MainActivity, "Failed to fetch user profile: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("FETCH_DATA", "Failed to fetch user profile", t)
                Toast.makeText(this@MainActivity, "Failed to fetch user profile: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatTime(createdAt: String): String {
        // UTC로 파싱한 뒤, 서울 시간대의 문자열로 포맷
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        val date: Date? = try {
            inputFormat.parse(createdAt)
        } catch (e: ParseException) {
            Toast.makeText(this, "Date parsing error", Toast.LENGTH_SHORT).show()
            return "알 수 없음"
        }

        val koreaDateTime = date?.let { outputFormat.format(it) }
        val now = Date()
        val nowInKorea = outputFormat.format(now)

        // 두 시간의 차이를 계산
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date1 = dateFormatter.parse(koreaDateTime)
        val date2 = dateFormatter.parse(nowInKorea)
        val durationMillis = (date2?.time ?: 0) - (date1?.time ?: 0)

        val minutes = durationMillis / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24

        // 차이를 문자열로 출력
        return when {
            days > 0 -> "$days 일 전"
            hours > 0 -> "$hours 시간 전"
            minutes > 0 -> "$minutes 분 전"
            else -> "방금 전"
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

}
