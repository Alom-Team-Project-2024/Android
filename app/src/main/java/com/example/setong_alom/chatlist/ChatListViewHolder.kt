package com.example.setong_alom.chatlist

import androidx.recyclerview.widget.RecyclerView
import com.example.setong_alom.databinding.ChatListBinding

class ChatListViewHolder(binding: ChatListBinding):RecyclerView.ViewHolder(binding.root) {
    val profileImageView = binding.profile
    val username = binding.userName
    val content = binding.content
    val time = binding.time
}