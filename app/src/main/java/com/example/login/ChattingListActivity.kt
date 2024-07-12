package com.example.login

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.login.databinding.ActivityChattingListBinding

class ChattingListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChattingListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChattingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val chattingList = ArrayList<ChattingList>()
        chattingList.add(ChattingList(R.drawable.profile))
        chattingList.add(ChattingList(R.drawable.profile))
        chattingList.add(ChattingList(R.drawable.profile))
        chattingList.add(ChattingList(R.drawable.profile))
        chattingList.add(ChattingList(R.drawable.profile))
        chattingList.add(ChattingList(R.drawable.profile))
        chattingList.add(ChattingList(R.drawable.profile))

        binding.rvchattinglist.adapter = ChattingListAdapter(chattingList)
        binding.rvchattinglist.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }
}