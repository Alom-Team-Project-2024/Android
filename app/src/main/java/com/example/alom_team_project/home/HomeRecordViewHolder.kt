package com.example.alom_team_project.home

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log

import android.util.TypedValue
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.alom_team_project.databinding.ItemHomeRecordBinding
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.question_board.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeRecordViewHolder(
    private val binding: ItemHomeRecordBinding,
    private val onItemClickListener: (Long) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun dpToPx(context: Context, dp: Int): Int {
        val resources = context.resources
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()
    }
    fun bind(record: HomeRecordData) {
        // 아이템 데이터 바인딩
        binding.title.text = record.title
        binding.commentCount.text = record.commentCount.toString()

        // username이 비어있지 않다면 사용자 정보를 가져옵니다
        val usernameToFetch = record.username ?: ""
        Log.d("DEBUG", "Fetching user info for username: $usernameToFetch")
        if (usernameToFetch.isNotEmpty()) {
            fetchUpdateUserInfo(usernameToFetch) { user ->
                if (user != null) {
                    Log.d("DEBUG", "Fetched user nickname: ${user.nickname}")
                    binding.mentorName.text = user.nickname
                } else {
                    Log.d("DEBUG", "User not found or error occurred")
                    binding.mentorName.text = "null"
                }
            }
        } else {
            // username이 비어있을 때 기본 처리
            Log.d("DEBUG", "Username is empty")
            binding.mentorName.text = "null"
            binding.mentorName.visibility = View.GONE
        }

        // answer가 null이거나 빈 문자열이면 뷰와 이미지뷰를 숨김
        binding.answer.text = record.answer ?: ""
        val isAnswerVisible = !record.answer.isNullOrEmpty()
        binding.answer.visibility = if (isAnswerVisible) View.VISIBLE else View.GONE
        binding.imgAnswer.visibility = if (isAnswerVisible) View.VISIBLE else View.GONE

        // 기타 데이터 바인딩
        binding.text.text = record.text

        // 이미지 로드
        if (record.images.isNotEmpty() && record.images[0].imageUrl.isNotEmpty()) {
            val imageUrl = record.images[0].imageUrl
            val fullImageUrl = "http://15.165.213.186/uploads/" + imageUrl

            Glide.with(itemView.context)
                .load(fullImageUrl)
                .transform(CenterCrop(), RoundedCorners(20))
                .into(binding.imageUrl)
            binding.imageUrl.visibility = View.VISIBLE
            binding.text.visibility = View.GONE
        } else {
            binding.text.visibility = View.VISIBLE
        }

        itemView.setOnClickListener {
            onItemClickListener(record.id)  // 클릭된 아이템의 ID 전달
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
