package com.example.alom_team_project

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alom_team_project.databinding.ActivityMainBinding
import com.example.alom_team_project.home.HomeRecordAdapter
import com.example.alom_team_project.home.HomeRecordData
import com.example.alom_team_project.home.NavigationFragment
import com.example.alom_team_project.job_board.MentorBoardActivity
import com.example.alom_team_project.job_board.MentorDetailFragment
import com.example.alom_team_project.login.UserApi
import com.example.alom_team_project.job_board.MentorPostResponse
import com.example.alom_team_project.mypage.UserResponse
import com.example.alom_team_project.question_board.AnswerFragment
import com.example.alom_team_project.question_board.ImageData
import com.example.alom_team_project.question_board.QuestionAdapterClass
import com.example.alom_team_project.question_board.QuestionClass
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var recordList: ArrayList<HomeRecordData>
    private lateinit var recordAdapter: HomeRecordAdapter
    private lateinit var tvHomeMore: TextView
    private var firstMentorId: Long? = null
    private var secondMentorId: Long? = null
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private val refreshInterval: Long = 12000 // 1분

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tvHomeMore = findViewById(R.id.tv_home_more)

        // 네비게이션 프래그먼트
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.navfragmentContainer, NavigationFragment())
        transaction.commit()

        tvHomeMore.setOnClickListener {
            navigateToMentorBoardActivity()
        }

        // Initialize recordList and recordAdapter
        recordList = ArrayList()
        recordAdapter = HomeRecordAdapter(recordList) { recordId ->
            // 기록 아이템 클릭 시 화면 전환
            val fragment = AnswerFragment().apply {
                arguments = Bundle().apply {
                    putLong("QUESTION_ID", recordId) // recordId를 QUESTION_ID로 전달
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.rvRecord.adapter = recordAdapter
        binding.rvRecord.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        fetchQuestions() // 홈화면 질문 불러오기
        fetchData() // 홈화면 이름 불러오기
        fetchMentors() // 홈화면 구인 불러오기

        updateRecordCount() // 초기화 시점에서 기록 개수를 업데이트합니다.

        // btn_container1 클릭 시 첫 번째 멘토 정보로 MentorDetailFragment로 이동
        binding.btnContainer1.setOnClickListener {
            firstMentorId?.let { mentorId ->
                openMentorDetailFragment(mentorId)
            }
        }

        // btn_container2 클릭 시 두 번째 멘토 정보로 MentorDetailFragment로 이동
        binding.btnContainer2.setOnClickListener {
            secondMentorId?.let { mentorId ->
                openMentorDetailFragment(mentorId)
            }
        }

        setupAutoRefresh()

    }

    private fun navigateToMentorBoardActivity() {
        val intent = Intent(this, MentorBoardActivity::class.java)
        startActivity(intent)
    }
    private fun fetchQuestions() {
        val token = getJwtToken()
        if (token != null) {
            RetrofitClient.userApi.getQuestions("Bearer $token").enqueue(object : Callback<List<QuestionClass>> {
                override fun onResponse(call: Call<List<QuestionClass>>, response: Response<List<QuestionClass>>) {
                    if (response.isSuccessful) {
                        response.body()?.let { questions ->
                            updateRecordList(questions)
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Failed to load questions", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<QuestionClass>>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun fetchnickname(username: String, callback: (String) -> Unit) {
        val token = getJwtToken() ?: return

        RetrofitClient.userApi.getUserProfile(username, "Bearer $token").enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    val nickname = user?.nickname ?: "No nickname"
                    callback(nickname) // 콜백을 통해 nickname 전달
                } else {
                    callback("No nickname") // 실패한 경우 기본 값 전달
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                callback("Error") // 실패 시 기본 값 설정
            }
        })
    }

    private fun updateRecordList(questions: List<QuestionClass>) {
        val maxItemCount = 10
        val tempList = mutableListOf<HomeRecordData>()
        val pendingRequests = mutableListOf<(String) -> Unit>()

        questions.forEach { question ->
            val replies = question.replies ?: emptyList()
            val images = question.images ?: emptyList()
            val mentorName = if (replies.isNotEmpty()) replies[0].writer else null
            var answer = if (replies.isNotEmpty()) replies[0].text else null
            val imageUrl = if (images.isNotEmpty()) images[0].imageUrl else ""
            val username = if (replies.isNotEmpty()) replies[0].username?.toString() else ""

            answer = answer?.let { insertSpacesEveryTwentyChars(it) }
            val text = insertSpacesEveryTwentyChars(question.text)

            if (username.isNotBlank() && replies.isEmpty()) {
                // fetchnickname 호출 후 데이터 추가
                pendingRequests.add { nickname ->
                    tempList.add(
                        HomeRecordData(
                            title = question.subject,
                            commentCount = question.replyCount,
                            images = listOf(ImageData(imageUrl)),
                            mentorName = nickname,
                            answer = answer,
                            id = question.id.toLong(),
                            text = text,
                            username = username
                        )
                    )
                    if (pendingRequests.isEmpty()) {
                        finalizeListUpdate(tempList, maxItemCount)
                    }
                }
                fetchnickname(username) { nickname ->
                    pendingRequests.forEach { it(nickname) }
                    pendingRequests.clear()
                }
            } else {
                val formattedMentorName = mentorName?.let {
                    "<b>$it</b> 멘토" // 멘토의 이름을 볼드 처리하고 뒤에 "멘토" 추가
                }

                tempList.add(
                    HomeRecordData(
                        title = question.subject,
                        commentCount = question.replyCount,
                        images = listOf(ImageData(imageUrl)),
                        mentorName = formattedMentorName,
                        answer = answer,
                        id = question.id.toLong(),
                        text = text,
                        username = username
                    )
                )
            }
        }

        // 모든 비동기 요청이 완료된 경우 어댑터 업데이트
        if (pendingRequests.isEmpty()) {
            finalizeListUpdate(tempList, maxItemCount)
        }
    }

    private fun finalizeListUpdate(tempList: MutableList<HomeRecordData>, maxItemCount: Int) {
        // 리스트가 최대 항목 수를 초과하는 경우 초과된 항목을 제거합니다.
        if (tempList.size > maxItemCount) {
            tempList.subList(maxItemCount, tempList.size).clear()
        }

        recordList.clear()
        recordList.addAll(tempList)

        // 어댑터에 데이터 변경 사항을 알립니다.
        recordAdapter.notifyDataSetChanged()

        // 기록 개수를 업데이트합니다.
        updateRecordCount()
    }



    private fun insertSpacesEveryTwentyChars(text: String, interval: Int = 20): String {
        val stringBuilder = StringBuilder(text)
        var index = interval
        while (index < stringBuilder.length) {
            stringBuilder.insert(index, "\n") // 20글자마다 줄바꿈 삽입
            index += interval + 1 // 줄바꿈 삽입 후 index 보정
        }
        return stringBuilder.toString()
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
            fetchnickname(mentor1.username) { nickname ->
                binding.tvName1.text = nickname
                binding.tvContent1.text = mentor1.text
                binding.tvTime1.text = formatTime(mentor1.createdAt)
            }

            // 첫 번째 멘토 ID 저장
            firstMentorId = mentor1.id
        }

        // 두 번째 아이템 업데이트
        if (sortedMentors.size > 1) {
            val mentor2 = sortedMentors[1]
            fetchnickname(mentor2.username) { nickname ->
                binding.tvName1.text = nickname
                binding.tvContent1.text = mentor2.text
                binding.tvTime1.text = formatTime(mentor2.createdAt)
            }

            // 두 번째 멘토 ID 저장
            secondMentorId = mentor2.id

        }

        // 첫 번째 멘토 클릭 리스너 설정
        binding.tvName1.setOnClickListener {
            firstMentorId?.let { mentorId ->
                openMentorDetailFragment(mentorId)
            }
        }

        // 두 번째 멘토 클릭 리스너 설정
        binding.tvName2.setOnClickListener {
            secondMentorId?.let { mentorId ->
                openMentorDetailFragment(mentorId)
            }
        }
    }

    private fun openMentorDetailFragment(mentorId: Long) {
        val fragment = MentorDetailFragment().apply {
            arguments = Bundle().apply {
                putLong("MENTOR_ID", mentorId)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.mainContainer, fragment)  // 프래그먼트를 담을 컨테이너 ID에 맞게 수정하세요.
            .addToBackStack(null)
            .commit()
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
}