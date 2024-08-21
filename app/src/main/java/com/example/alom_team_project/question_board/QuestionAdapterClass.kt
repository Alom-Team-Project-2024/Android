package com.example.alom_team_project.question_board

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.alom_team_project.R
import com.example.alom_team_project.databinding.QuestionBoardItemBinding
import com.example.alom_team_project.question_board.QuestionClass
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

class QuestionAdapterClass(private val questionList: ArrayList<QuestionClass>) :
    RecyclerView.Adapter<QuestionAdapterClass.ViewHolder>() {

    private var filteredList: ArrayList<QuestionClass> = ArrayList(questionList)

    init {
        filteredList = ArrayList(questionList) // 초기화 시 filteredList에 questionList를 할당
    }


    class ViewHolder(private val binding: QuestionBoardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(question: QuestionClass) {

            val utcTime = question.createdAt
            Log.d("zzzz", utcTime.toString())

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
            binding.subjectName.text = question.subject
            binding.content.text = question.text

            binding.likeNum.text = question.likes.toString()
            binding.commentNum.text = question.replyCount.toString()
            binding.scrapNum.text = question.scrapCount.toString()

            // Button 스타일 설정 (필요에 따라 조정)
            binding.likeButton.setBackgroundResource(R.drawable.like_button)
            binding.commentButton.setBackgroundResource(R.drawable.comment_button)
            binding.scrapButton.setBackgroundResource(R.drawable.scrap_button)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = QuestionBoardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = filteredList[position]

        //holder.uploadTime.text = elapsedTime

        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    fun filter(query: String) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())
        filteredList = if (query.isEmpty()) {
            ArrayList(questionList)  // 전체 리스트를 복사
        } else {
            questionList.filter {
                it.subject.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
            } as ArrayList<QuestionClass>
        }
        notifyDataSetChanged()  // 데이터 변경 알림
    }

}
