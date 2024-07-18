package com.example.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.login.databinding.ActivityChatListBinding

class ChatListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val chatList = ArrayList<ChatList>()
        chatList.add(ChatList(R.drawable.profile, "이수민", "안녕하세요", "1분 전"))
        chatList.add(ChatList(R.drawable.profile, "이수민", "안녕하세요", "1분 전"))
        chatList.add(ChatList(R.drawable.profile, "세종이", "안녕하세요", "1분 전"))
        chatList.add(ChatList(R.drawable.profile, "세종이", "안녕하세요", "1분 전"))
        chatList.add(ChatList(R.drawable.profile, "김나영", "안녕하세요", "1분 전"))
        chatList.add(ChatList(R.drawable.profile, "김나영", "안녕하세요", "1분 전"))
        chatList.add(ChatList(R.drawable.profile, "김나영", "안녕하세요", "1분 전"))

        val adapter = ChatListAdapter(chatList)
        binding.rcvChattinglist.adapter = adapter
        binding.rcvChattinglist.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // 클릭 리스너 설정
        adapter.setOnItemClickListener(object : ChatListAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // 클릭된 아이템의 포지션을 이용하여 처리할 내용을 작성
                val clickedItem = chatList[position]
                // 채팅 액티비티로 이동
                val intent = Intent(this@ChatListActivity, ChatActivity::class.java)
                startActivity(intent)
            }
        })
    }
}
