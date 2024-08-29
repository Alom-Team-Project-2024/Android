package com.example.alom_team_project.chat.dialog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.alom_team_project.R
import com.example.alom_team_project.chat.ChatFragment
import com.example.alom_team_project.databinding.ActivityUserProfileBinding

class UserProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // 뒤로 가기 버튼, 확인 버튼 클릭 시 다시 채팅방으로 돌아가기
        binding.backIcon.setOnClickListener {
            openChatPage()
        }
        binding.okBtn.setOnClickListener {
            openChatPage()
        }
    }

    private fun openChatPage() {
        val fragment = ChatFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.chatPage, fragment)
            .addToBackStack(null)
            .commit()
    }
}