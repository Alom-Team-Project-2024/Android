package com.example.setong_alom

import CustomDialogC
import CustomDialogR
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RatingBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.setong_alom.databinding.ActivityChatBinding
import com.example.setong_alom.databinding.AssessDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var assessDialog: AssessDialogBinding
    private val chattingList = ArrayList<ChatData>()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUserProfile()
        setupRecyclerView()

        // 전송 버튼
        binding.sendBtn.setOnClickListener {
            // 메시지 화면에 띄우기
            if (binding.etMessage.length() > 0) {
                chattingList.add(ChatData(binding.etMessage.text.toString(), 1))
                adapter.notifyDataSetChanged()
                binding.etMessage.setText("")
                binding.rcvChatting.scrollToPosition(chattingList.size - 1)
            }

        }

        // 뒤로 가기 버튼
        binding.backIcon.setOnClickListener {
            val intent = Intent(this, ChatListActivity::class.java)
            startActivity(intent)
        }

        // Bottom Sheet 설정
        setupBottomSheet()

        // 메뉴 버튼 클릭 시 Bottom Sheet 보이기
        binding.menuBtn.setOnClickListener {
            bottomSheetDialog.show()
        }

        // 프로필 사진, 닉네임 클릭 시 유저 프로픽 액티비티로 이동
        binding.profileImg.setOnClickListener {
            val intent = Intent(this,UserProfileActivity::class.java)
            startActivity(intent)
        }
        binding.nicknameTv.setOnClickListener {
            val intent = Intent(this,UserProfileActivity::class.java)
            startActivity(intent)
        }

    }

    // 프로필 사진, 닉네임 불러오기
    private fun setupUserProfile() {
        val token = TokenManager.getToken(this)

        // 토큰이 null인지 확인
        if (token != null) {
            val userId = "2" // 상대방 ID 설정

            val userInfo = RetrofitClient.instance.create(ChatService::class.java)
            val call = userInfo.getUserInfo(token, userId)

            call.enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        // 성공적인 응답 처리
                        val nickname = response.body()?.nickname
                        val profileImg = response.body()?.profileImage

                        Log.d("Chat", "Nickname: $nickname")

                        // 닉네임, 프로필 사진 UI 업데이트
                        binding.nicknameTv.text = nickname

                    } else {
                        // 응답 실패 처리
                        Log.e("API Error", "Response Code: ${response.code()}, Message: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    // 요청 실패 처리
                    Log.e("API Error", "Failure: ${t.message}")
                }
            })
        } else {
            // 토큰이 null인 경우 처리
            Log.e("Token Error", "Token is null")
        }
    }

    private fun setupRecyclerView() {
        // 어댑터 연결
        adapter = ChatAdapter(chattingList)
        binding.rcvChatting.adapter = adapter
        binding.rcvChatting.layoutManager = LinearLayoutManager(this)
    }

    // 메뉴 바텀 시트 띄우기
    private fun setupBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.chat_btm_sheet, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        // 평가하기 다이얼로그
        bottomSheetView.findViewById<AppCompatButton>(R.id.rating_btn).setOnClickListener {
            bottomSheetDialog.dismiss()
            showRatingDialog()
        }

        // 매칭 여부 확인 다이얼로그
        bottomSheetView.findViewById<AppCompatButton>(R.id.success_btn).setOnClickListener {
            bottomSheetDialog.dismiss()
            showConfirmDialog()
        }
    }

    private fun showConfirmDialog() {
        val dialogViewC = layoutInflater.inflate(R.layout.confirm_dialog, null)
        val dialogC = CustomDialogC(this)

        dialogC.setItemClickListener(object : CustomDialogC.ItemClickListener {
            override fun onClick(message: String) {
                //
            }
        })
        dialogC.show()
    }

    // 평가하기 다이얼로그 띄우기
    private fun showRatingDialog() {
        val dialogR = CustomDialogR(this)
        dialogR.setContentView(R.layout.assess_dialog)

        val ratingBar = dialogR.findViewById<RatingBar>(R.id.ratingBar)
        val button3 = dialogR.findViewById<Button>(R.id.button3)
        val buttonX = dialogR.findViewById<Button>(R.id.buttonX)

        dialogR.show()
    }
}
