import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.databinding.DataBindingUtil.setContentView
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.chat.ChatService
import com.example.alom_team_project.databinding.AssessDialogBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomDialogR(context: Context, chatRoomId: Long, nickname: String) : Dialog(context) {
    private lateinit var itemClickListener: ItemClickListener
    private lateinit var binding: AssessDialogBinding
    private var chatNick: String = ""

    val nickname = nickname

    // SharedPreferences 키
    private val PREF_NAME = "rate_pref"
    private val PREF_KEY_HAS_RATED = "has_rated_${chatRoomId}_${nickname}" // 각 nickname 별로 저장

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AssessDialogBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // SharedPreferences에서 JWT 토큰을 가져옴
        val token = getJwtToken()

        // SharedPreferences에서 hasRated 값을 불러옴
        val hasRated = getHasRated()
        Log.d("temperature", "hasRated: $hasRated")

        // 배경을 투명하게 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 다이얼로그 바깥쪽 클릭시 닫기
        setCanceledOnTouchOutside(true)

        // 취소 가능 여부
        setCancelable(true)

        binding.textView.text = "${nickname}님을 평가하시겠습니까?"
        binding.textView3.text = "${nickname}님에게 하고 싶은 말을 전해보세요!"
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
                if (!hasRated) {
                    Log.d("temperature", "$chatNick")
                    rateStar("Bearer $token", chatNick, binding.ratingBar.rating.toInt())
                }
                else {
                    Toast.makeText(context, "이미 평가를 완료했습니다.", Toast.LENGTH_SHORT).show()
                }
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
    private fun rateStar(token: String, nick: String, rating: Int) {
        val ratingService = RetrofitClient.instance.create(ChatService::class.java)
        val call = ratingService.userRate(token, nick, rating)
        Log.d("temperature", "$token / $nick / $rating")

        call.enqueue(object : Callback<Double> {
            override fun onResponse(call: Call<Double>, response: Response<Double>) {
                if (response.isSuccessful) {
                    val rateResponse = response.body()
                    Log.d("temperature", "rate : $rateResponse")
                    // 평가 후에 hasRated 값을 true로 저장
                    saveHasRated(true)
                } else {
                    // 응답 실패 처리
                    Log.e("API ErrorT", "Response Code: ${response.code()}, Message: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Double>, t: Throwable) {
                // 요청 실패 처리
                Log.e("API ErrorT", "Failure: ${t.message}")
            }
        })
    }

    // JWT 토큰을 SharedPreferences에서 가져오는 메서드 추가
    private fun getJwtToken(): String? {
        val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }

    // hasRated 값을 SharedPreferences에 저장
    private fun saveHasRated(hasRated: Boolean) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean(PREF_KEY_HAS_RATED, hasRated)
            apply()
        }
    }

    // hasRated 값을 SharedPreferences에서 불러옴
    private fun getHasRated(): Boolean {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getBoolean(PREF_KEY_HAS_RATED, false)
    }
}
