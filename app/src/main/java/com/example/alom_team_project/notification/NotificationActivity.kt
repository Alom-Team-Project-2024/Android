package com.example.alom_team_project.notification

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alom_team_project.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private lateinit var adapter: NotificationAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val notificationList = ArrayList<NotificationData>().apply {
            add(NotificationData("세종이님이 좋아요를 누르셨습니다.","1분 전", false))
            add(NotificationData("세종이님이 좋아요를 누르셨습니다.","1분 전", false))
            add(NotificationData("세종이님이 좋아요를 누르셨습니다.","1분 전", false))
            add(NotificationData("세종이님이 좋아요를 누르셨습니다.","1분 전", false))
            add(NotificationData("세종이님이 좋아요를 누르셨습니다.","1분 전", false))
            add(NotificationData("세종이님이 좋아요를 누르셨습니다.","1분 전", false))
            add(NotificationData("세종이님이 좋아요를 누르셨습니다.","1분 전", false))
        }

        adapter = NotificationAdapter(notificationList)
        binding.rcvNtflist.adapter = adapter
        binding.rcvNtflist.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // 클릭 리스너 설정
        adapter.setOnItemClickListener(object : NotificationAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val clickedItem = notificationList[position]
                clickedItem.isRead = true // 항목을 읽음 상태로 변경
                adapter.notifyItemChanged(position) // 변경된 항목을 업데이트
            }
        })
    }
}