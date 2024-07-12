package com.example.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.login.databinding.ActivityChattingBinding

class ChattingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChattingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChattingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val chattingList = ArrayList<ChattingData>()
        chattingList.add(
            ChattingData(
                "안녕하세요",
                0
            )
        )
        chattingList.add(
            ChattingData(
                "안녕하세요",
                1
            )
        )
        binding.rvChatting.adapter = ChattingAdapter(chattingList)
        val adapter = binding.rvChatting.adapter
        binding.rvChatting.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        binding.backIcon.setOnClickListener {
            val intent = Intent(this, ChattingListActivity::class.java)
            startActivity(intent)
        }

    }
}