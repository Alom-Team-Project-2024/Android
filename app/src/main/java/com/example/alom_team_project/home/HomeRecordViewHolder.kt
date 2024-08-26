package com.example.alom_team_project.home

import androidx.recyclerview.widget.RecyclerView
import com.example.alom_team_project.databinding.ItemHomeRecordBinding


class HomeRecordViewHolder(binding: ItemHomeRecordBinding): RecyclerView.ViewHolder(binding.root){
    val title=binding.title
    val commentCount=binding.commentCount
    val imageUrl=binding.imageUrl
    val mentorName=binding.mentorName
    val answer=binding.answer
}
