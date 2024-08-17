package com.example.setong_alom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.setong_alom.databinding.ActivityUserProfileBinding

class UserProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로 가기 버튼, 확인 버튼 클릭 시 다시 채팅방으로 돌아가기
        binding.backIcon.setOnClickListener {
            val intent = Intent(this,ChatActivity::class.java)
            startActivity(intent)
        }
        binding.okBtn.setOnClickListener {
            val intent = Intent(this,ChatActivity::class.java)
            startActivity(intent)
        }
    }
}