package com.example.login

import androidx.recyclerview.widget.RecyclerView
import com.example.login.databinding.ChattingListBinding

class ChatListViewHolder(binding: ChattingListBinding):RecyclerView.ViewHolder(binding.root) {
    val profileImageView = binding.profile
    val username = binding.userName
    val content = binding.content
    val time = binding.time
}