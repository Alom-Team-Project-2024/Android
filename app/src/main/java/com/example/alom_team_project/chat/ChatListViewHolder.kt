package com.example.alom_team_project.chat

import androidx.recyclerview.widget.RecyclerView
import com.example.alom_team_project.databinding.ChatListBinding

class ChatListViewHolder(binding: ChatListBinding): RecyclerView.ViewHolder(binding.root) {
    val profileImageView = binding.profile
    val username = binding.userName
    val content = binding.content
    val time = binding.time
}