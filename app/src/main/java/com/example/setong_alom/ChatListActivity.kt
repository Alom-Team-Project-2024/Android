package com.example.setong_alom

import ChatListAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.setong_alom.databinding.ActivityChatListBinding

class ChatListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatListBinding
    private lateinit var adapter: ChatListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val chatList = ArrayList<ChatList>().apply {
            add(ChatList(R.drawable.profile, "이수민", "안녕하세요", "1분 전"))
            add(ChatList(R.drawable.profile, "이수민", "안녕하세요", "1분 전"))
            add(ChatList(R.drawable.profile, "세종이", "안녕하세요", "1분 전"))
            add(ChatList(R.drawable.profile, "세종이", "안녕하세요", "1분 전"))
            add(ChatList(R.drawable.profile, "김나영", "안녕하세요", "1분 전"))
            add(ChatList(R.drawable.profile, "김나영", "안녕하세요", "1분 전"))
            add(ChatList(R.drawable.profile, "김나영", "안녕하세요", "1분 전"))
            add(ChatList(R.drawable.profile, "김 나영", "안녕하세요", "1분 전"))
        }

        adapter = ChatListAdapter(chatList)
        binding.rcvChattinglist.adapter = adapter
        binding.rcvChattinglist.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // 클릭 리스너 설정
        adapter.setOnItemClickListener(object : ChatListAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val clickedItem = chatList[position]
                val intent = Intent(this@ChatListActivity, ChatActivity::class.java)
                // 선택한 채팅 정보를 ChatActivity에 전달하고 싶다면 추가 데이터를 포함할 수 있습니다.
                startActivity(intent)
            }
        })

        // EditText의 텍스트 변화 리스너 설정
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 텍스트 변화 전의 행동을 정의
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트가 변경될 때마다 어댑터의 필터링 메소드 호출
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // 텍스트 변화 후의 행동을 정의
            }
        })
    }
}
