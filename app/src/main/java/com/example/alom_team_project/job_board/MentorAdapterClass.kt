package com.example.alom_team_project.job_board

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.databinding.MentorBoardItemBinding
import com.example.alom_team_project.question_board.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
class MentorAdapterClass(
    private val mentorList: ArrayList<MentorClass>,
    private val onItemClickListener: (Long) -> Unit // 클릭 리스너 추가
) : RecyclerView.Adapter<MentorAdapterClass.ViewHolder>() {

    private var filteredList: ArrayList<MentorClass> = ArrayList(mentorList)

    init {
        filteredList = ArrayList(mentorList)
    }


    class ViewHolder(
        private val binding: MentorBoardItemBinding,
        private val onItemClickListener: (Long) -> Unit // 클릭 리스너를 생성자에 추가
    ) : RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(mentor: MentorClass) {

            val utcTime = mentor.createdAt

            // DateTimeFormatter를 커스터마이즈하여 오프셋 없는 포맷을 처리
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

            // UTC로 파싱한 뒤, 서울 시간대로 변환
            val utcDateTime = LocalDateTime.parse(utcTime.toString(), formatter).atZone(ZoneId.of("UTC"))
            val koreaDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"))

            // 현재 디바이스의 시간을 서울 시간 기준으로 가져오기
            val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))

            // 두 시간의 차이를 계산
            val duration = Duration.between(koreaDateTime, now)

            // 차이를 원하는 형식으로 변환
            val days = duration.toDays()
            val hours = duration.toHours() % 24
            val minutes = duration.toMinutes() % 60

            // 차이를 문자열로 출력
            val elapsedTime = when {
                days > 0 -> "$days 일 전"
                hours > 0 -> "$hours 시간 전"
                minutes > 0 -> "$minutes 분 전"
                else -> "방금 전"
            }

            binding.postTime.text = elapsedTime
            binding.title.text = mentor.title
            binding.content.text = mentor.text


            binding.scrapNum.text = mentor.scrapCount.toString()
            binding.scrapButton.setBackgroundResource(R.drawable.scrap_button2)

            // 아이템 클릭 리스너 설정
            itemView.setOnClickListener {
                // 키보드를 숨기기 위해 InputMethodManager 사용
                val imm = binding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(itemView.windowToken, 0)

                onItemClickListener(mentor.id)  // 클릭된 아이템의 ID 전달
            }

            // 질문자 닉네임 설정
            fetchUpdateUserInfo(mentor.username) { user ->
                if (user != null) {
                    binding.nickname.text = user.nickname
                }
            }
        }

        private fun fetchUpdateUserInfo(username: String, callback: (User?) -> Unit) {
            val token = getJwtToken()

            // 프로필 정보 가져오기 요청
            RetrofitClient.service.getProfile("Bearer $token", username).enqueue(object :
                Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        // 성공적으로 사용자 프로필 정보를 받았을 때
                        val user = response.body()
                        callback(user)  // JSON 데이터 반환 (User 객체)
                    } else {
                        // 요청이 실패했을 때
                        Log.e("UserProfile", "Error: ${response.code()} - ${response.message()}")
                        callback(null)  // 실패 시 null 반환
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    // 네트워크 오류나 다른 문제가 발생했을 때
                    Log.e("UserProfile", "Request failed", t)
                    callback(null)  // 오류 시 null 반환
                }
            })
        }

        private fun getJwtToken(): String {
            val sharedPref = binding.root.context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            return sharedPref.getString("jwt_token", "") ?: ""
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MentorBoardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding,onItemClickListener)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = filteredList[position]

        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    fun filter(query: String) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())
        filteredList = if (query.isEmpty()) {
            ArrayList(mentorList)  // 전체 리스트를 복사
        } else {
            mentorList.filter {
                it.title.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
            } as ArrayList<MentorClass>
        }
        notifyDataSetChanged()  // 데이터 변경 알림
    }

}
