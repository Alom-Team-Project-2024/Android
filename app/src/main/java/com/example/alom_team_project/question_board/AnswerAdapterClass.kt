package com.example.alom_team_project.question_board


import android.content.Context
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
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
            binding.likeNum.text = answer.likes.toString()


            // SharedPreferences 가져오기
            val sharedPreferences =
                binding.root.context.getSharedPreferences("like_prefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            val isLiked = sharedPreferences.getBoolean("liked_${answer.id}", false)

            val displayMetrics = binding.root.context.resources.displayMetrics
            val density = displayMetrics.density

// CardView 크기 및 여백 설정 (375dp x 222dp)
            val cardWidth = (375 * density).toInt()
            val cardHeight = (222 * density).toInt()

            if (answer.images.isNotEmpty()) {
                binding.imageContainer.visibility = View.VISIBLE
                binding.imageContainer.removeAllViews()

                for (image in answer.images) {
                    val imageUrl = image.imageUrl
                    val fullImageUrl = "http://15.165.213.186/uploads/" + imageUrl

                    Log.d("GlideImageURL", "Loading image URL: $fullImageUrl")

                    // 1. CardView 생성
                    val cardView = CardView(binding.root.context).apply {
                        radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, displayMetrics)
                        cardElevation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, displayMetrics)
                        layoutParams = LinearLayout.LayoutParams(cardWidth, cardHeight).apply {
                            setMargins(
                                (8 * density).toInt(), // 좌우 여백 설정 (밀도 적용)
                                (8 * density).toInt(),
                                (8 * density).toInt(),
                                (8 * density).toInt()
                            )
                        }
                    }

                    // 2. ImageView 생성
                    val imageView = ImageView(binding.root.context).apply {
                        layoutParams = LinearLayout.LayoutParams(cardWidth, cardHeight)
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }

                    // 3. Glide로 이미지 로드
                    Glide.with(binding.root.context)
                        .load(fullImageUrl)
                        .apply(
                            RequestOptions()
                                .override(cardWidth, cardHeight)  // 밀도를 고려한 크기 설정
                                .transform(RoundedCorners(70))
                                .centerCrop()
                        )
                        .into(imageView)

                    // 4. ImageView를 CardView에 추가
                    cardView.addView(imageView)

                    // 5. CardView를 imageContainer에 추가
                    binding.imageContainer.addView(cardView)
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

            // 질문자 프로필 설정
            fetchUpdateUserInfo(answer.username) { user ->
                if (user != null) {
                    // 사용자 닉네임과 프로필 이미지를 UI에 설정
                    binding.answererName.text = user.nickname

                    // profileImage가 null인지 먼저 체크
                    val profileImage = user.profileImage
                    if (!profileImage.isNullOrEmpty()) {
                        val fullImageUrl = "http://15.165.213.186/$profileImage"
                        Glide.with(binding.root.context)
                            .load(fullImageUrl)
                            .apply(RequestOptions.circleCropTransform())
                            .into(binding.answererProfile)
                    } else {
                        // 프로필 이미지가 없을 경우 기본 이미지 설정
                        binding.answererProfile.setImageResource(R.drawable.group_172)
                    }
                } else {
                    // 실패 시 UI 업데이트 처리 (필요시)
                    binding.answererProfile.setImageResource(R.drawable.group_172)  // 기본 이미지 설정
                }
            }
        }

        private fun fetchUpdateUserInfo(username: String, callback: (User?) -> Unit) {
            val token = getJwtToken()

            // 프로필 정보 가져오기 요청
            RetrofitClient.service.getProfile("Bearer $token", username).enqueue(object : Callback<User> {
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