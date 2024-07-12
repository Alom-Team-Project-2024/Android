package com.example.alom_team_project

import androidx.recyclerview.widget.RecyclerView
import com.example.alom_team_project.databinding.ItemHomeRecordBinding


class HomeRecordViewHolder(binding: ItemHomeRecordBinding): RecyclerView.ViewHolder(binding.root){
    val title=binding.title
    val status=binding.status
    val imageUrl=binding.imageUrl
    val mentorName=binding.mentorName
    val answer=binding.answer
}
