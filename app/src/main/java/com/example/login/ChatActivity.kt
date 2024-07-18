package com.example.login

import CustomDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.login.databinding.ActivityChatBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private val chattingList = ArrayList<ChatData>()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        // 전송 버튼
        binding.sendBtn.setOnClickListener {
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
    }

    private fun setupRecyclerView() {
        chattingList.add(ChatData("안녕하세요", 0))
        chattingList.add(ChatData("안녕하세요", 1))

        adapter = ChatAdapter(chattingList)
        binding.rcvChatting.adapter = adapter
        binding.rcvChatting.layoutManager = LinearLayoutManager(this)
    }

    private fun setupBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.chat_btm_sheet, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        bottomSheetView.findViewById<AppCompatButton>(R.id.rating_btn).setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetView.findViewById<AppCompatButton>(R.id.success_btn).setOnClickListener {
            bottomSheetDialog.dismiss()
            showSuccessDialog()
        }
    }

    private fun showSuccessDialog() {
        val dialogViewS = layoutInflater.inflate(R.layout.confirm_dialog, null)
        val dialogS = CustomDialog(this)

        dialogS.setItemClickListener(object : CustomDialog.ItemClickListener {
            override fun onClick(message: String) {
                //
            }
        })
        dialogS.show()
    }

}
