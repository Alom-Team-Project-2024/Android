package com.example.alom_team_project.question_board


import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.databinding.AnswerItemBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class AnswerAdapterClass(
    private val answerList: ArrayList<Reply>,
    private val onLikeClicked: (Reply, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class AnswerViewHolder(private val binding: AnswerItemBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(answer: Reply,onLikeClicked: (Reply, Int) -> Unit) {

            val utcTime = answer.createdAt

            // DateTimeFormatter를 커스터마이즈하여 오프셋 없는 포맷을 처리
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

            // UTC로 파싱한 뒤, 서울 시간대로 변환
            val utcDateTime =
                LocalDateTime.parse(utcTime.toString(), formatter).atZone(ZoneId.of("UTC"))
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


            // SharedPreferences 가져오기
            val sharedPreferences =
                binding.root.context.getSharedPreferences("like_prefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            val isLiked = sharedPreferences.getBoolean("liked_${answer.id}", false)

            if (answer.images.isNotEmpty()) {
                binding.imageContainer.visibility = View.VISIBLE
                binding.imageContainer.removeAllViews()

                for (image in answer.images) {
                    val imageUrl = image.imageUrl
                    val fullImageUrl = "http://15.165.213.186/uploads/" + imageUrl

                    // URL 로그 출력
                    Log.d("GlideImageURL", "Loading image URL: $fullImageUrl")

                    val imageView = ImageView(binding.root.context)
                    val layoutParams = LinearLayout.LayoutParams(
                        150, // 너비를 150px로 설정
                        150  // 높이를 150px로 설정
                    ).apply {
                        setMargins(4, 4, 4, 4) // 여백 설정
                    }
                    imageView.layoutParams = layoutParams
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP // 이미지 중앙을 기준으로 자르기

                    Glide.with(binding.root.context)
                        .load(fullImageUrl)
                        .apply(
                            RequestOptions()
                            .override(150, 150)  // Glide에서 로드할 이미지의 크기를 150x150 픽셀로 설정
                            .centerCrop()  // 중앙을 기준으로 자르기
                        )
                        .into(imageView)

                    binding.imageContainer.addView(imageView)
                }
            } else {
                binding.imageContainer.visibility = View.GONE
            }


            // 좋아요 버튼 이미지 설정
            binding.likeButton.setBackgroundResource(
                if (isLiked) R.drawable.like_button2 else R.drawable.like_button
            )

            // 좋아요 버튼 클릭 리스너
            binding.likeButton.setOnClickListener {
                onLikeClicked(answer, adapterPosition)

                // 서버에 좋아요 요청 보내기
                postLike(answer.id)

                // 좋아요 이미지 변경 및 상태 저장
                val newIsLiked = true
                binding.likeButton.setBackgroundResource(
                    if (newIsLiked) R.drawable.like_button2 else R.drawable.like_button
                )
                editor.putBoolean("liked_${answer.id}", newIsLiked)
                editor.apply()

                binding.likeButton.isEnabled = true
            }
        }

        private fun postLike(replyId: Long) {
            val token = getJwtToken()
            val service = RetrofitClient.service

            service.likeReply("Bearer $token", replyId).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        //Log.d("LIKE", "Successfully liked the post")
                    } else {
                        //Log.e("LIKE", "Failed to like the post: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("LIKE", "Error occurred while liking the post", t)
                }
            })
        }

        private fun getJwtToken(): String {
            val sharedPref = binding.root.context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            return sharedPref.getString("jwt_token", "") ?: ""
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
        //val currentItem = answerList[position]

        if (holder is AnswerViewHolder) {
            holder.bind(answerList[position], onLikeClicked)
            //holder.itemView.requestLayout()
        }
    }
}