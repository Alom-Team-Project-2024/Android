package com.example.alom_team_project.chat.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.chat.ChatService
import com.example.alom_team_project.databinding.AssessDialogBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomDialogR(context: Context) : Dialog(context) {
    private lateinit var itemClickListener: ItemClickListener
    private lateinit var binding: AssessDialogBinding
    private var chatNick: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AssessDialogBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // SharedPreferences에서 JWT 토큰을 가져옴
        val token = getJwtToken()

        // 배경을 투명하게 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 다이얼로그 바깥쪽 클릭시 닫기
        setCanceledOnTouchOutside(true)

        // 취소 가능 여부
        setCancelable(true)

        binding.buttonX.setOnClickListener {
            dismiss()
        }

        binding.button3.isEnabled = false
        binding.button3.setBackgroundColor(Color.parseColor("#FFFFFF"))

        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            if (rating > 0) {
                binding.button3.isEnabled = true
                binding.button3.setBackgroundColor(Color.parseColor("#D9D9D9"))
            } else {
                binding.button3.isEnabled = false
                binding.button3.setBackgroundColor(Color.WHITE)
            }
        }

        binding.button3.setOnClickListener {
            // 별점과 코멘트 서버로 전송
            if (token != null) {
                Log.d("temperature", "$chatNick")
                rateStar("Bearer $token", chatNick, binding.ratingBar.rating.toInt())
            }
            dismiss()
        }
    }

    interface ItemClickListener {
        fun onClick(message: String)
    }

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    fun setChatNick(nick: String) {
        chatNick = nick
    }

    // 서버로 별점 보내기
    fun rateStar(token: String, nick: String, rating: Int) {
        val ratingService = RetrofitClient.instance.create(ChatService::class.java)
        val call = ratingService.userRate("Bearer $token", nick, rating)
        Log.d("temperature", "$nick / $rating")

        call.enqueue(object : Callback<Double> {
            override fun onResponse(call: Call<Double>, response: Response<Double>) {
                if (response.isSuccessful) {
                    val rateResponse = response.body()
                    Log.d("tem", "$rateResponse")
                } else {
                    // 응답 실패 처리
                    Log.e("API Error", "Response Code: ${response.code()}, Message: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Double>, t: Throwable) {
                // 요청 실패 처리
                Log.e("API Error", "Failure: ${t.message}")
            }
        })
    }

    // JWT 토큰을 SharedPreferences에서 가져오는 메서드 추가
    private fun getJwtToken(): String? {
        val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }

    // 유저네임을 SharedPreferences에서 가져오는 메서드 추가
    private fun getUsername(): String? {
        val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPref.getString("username", null)
    }
}
