package com.example.alom_team_project.question_board

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.alom_team_project.R
import com.example.alom_team_project.databinding.AnswerItemBinding
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class AnswerAdapterClass(val answerList: ArrayList<Reply>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class AnswerViewHolder(private val binding: AnswerItemBinding):RecyclerView.ViewHolder(binding.root){

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(answer: Reply) {

            val utcTime = answer.createdAt
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

            binding.answerTime.text = elapsedTime
            binding.answerText.text = answer.text
            binding.answererName.text = answer.writer
            binding.likeNum.text = answer.likes.toString()

            binding.likeButton.setBackgroundResource(R.drawable.like_button)

            // 이미지가 없으면 ImageView를 GONE으로 설정
            if (answer.images.isNullOrEmpty()) {
                binding.answerImage.visibility = View.GONE
            } else {
                binding.answerImage.visibility = View.VISIBLE
                // 이미지가 있으면 이미지를 설정할 수 있습니다. 예: Glide를 사용하여 이미지 로드
                // Glide.with(binding.answerImage.context).load(answer.images[0].url).into(binding.answerImage)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val binding = AnswerItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return AnswerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return answerList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = answerList[position]

        if (holder is AnswerViewHolder) {
            holder.bind(currentItem)
        }
    }
}