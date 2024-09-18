package com.example.alom_team_project.home

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
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

        fetchUpdateUserInfo(usernameToFetch) { user ->
            if (user != null) {
                Log.d("DEBUG", "Fetched user nickname: ${user.nickname}")
                // 멘토 이름을 볼드 처리하고 '멘토'를 붙입니다
                val formattedName = "<b>${user.nickname}</b> 멘토"

                // API 레벨에 따라 Html.fromHtml 처리
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    binding.mentorName.text = Html.fromHtml(formattedName, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    binding.mentorName.text = Html.fromHtml(formattedName)
                }

                // 멘토 이름을 확실하게 보이도록 설정
                binding.mentorName.visibility = View.VISIBLE
            } else {
                Log.d("DEBUG", "Username is empty")
                binding.mentorName.text = "null"
                binding.mentorName.visibility = View.GONE
                }
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

            // 로그를 추가하여 이미지 URL, 텍스트, 제목 확인
            Log.d("HomeRecordViewHolder", "Loading image: $fullImageUrl, Title: ${record.title}, Text: ${record.text}")

            Glide.with(itemView.context)
                .load(fullImageUrl)
                .transform(CenterCrop(), RoundedCorners(50))
                .into(binding.imageUrl)

            // 이미지가 있을 때만 이미지를 표시하고 텍스트를 숨깁니다.
            binding.imageUrl.visibility = View.VISIBLE
            binding.text.visibility = View.GONE

            // 로그를 추가하여 현재 이미지 뷰와 텍스트 뷰의 상태 확인
            Log.d("HomeRecordViewHolder", "Image is visible. Text is hidden. Title: ${record.title}, Text: ${record.text}")
        } else {
            Glide.with(itemView.context)
                .load(binding.imageUrl)
                .into(binding.imageUrl)
            binding.imageUrl.visibility=View.VISIBLE
            // 이미지가 없을 때 텍스트를 표시하고 이미지를 숨깁니다.
            binding.text.visibility = View.VISIBLE

            // 로그를 추가하여 텍스트와 제목 확인
            Log.d("HomeRecordViewHolder", "No image found. Displaying text: ${record.text}, Title: ${record.title}")

            // 로그를 추가하여 현재 텍스트 뷰와 이미지 뷰의 상태 확인
            Log.d("HomeRecordViewHolder", "Text is visible. Image is hidden. Title: ${record.title}, Text: ${record.text}")
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
