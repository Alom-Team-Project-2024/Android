package com.example.alom_team_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alom_team_project.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var recordList: ArrayList<HomeRecordData>
    private lateinit var recordAdapter: HomeRecordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recordList = ArrayList()
        recordList.add(
            HomeRecordData(
                "인공지능과 빅데이터",
                "답변완료",
                "https://example.com/image1.jpg", // 이미지 URL 예시
                "세종이멘토",
                "이것은 답변 예시입니다."
            )
        )
        recordList.add(
            HomeRecordData(
                "현대인의 정신건강과 자기발견",
                "진행중",
                "https://example.com/image1.jpg", // 이미지 URL 예시
                "세종이멘토",
                "이것은 답변 예시입니다."
            )
        )

        recordAdapter = HomeRecordAdapter(recordList)
        binding.rvRecord.adapter = recordAdapter
        binding.rvRecord.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

    }
}
