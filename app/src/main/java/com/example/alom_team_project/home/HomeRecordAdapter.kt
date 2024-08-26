package com.example.alom_team_project.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.alom_team_project.databinding.ItemHomeRecordBinding


class HomeRecordAdapter(val recordList:ArrayList<HomeRecordData>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding= ItemHomeRecordBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return HomeRecordViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return recordList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HomeRecordViewHolder) {
            holder.title.text = recordList[position].title
            holder.commentCount.text = recordList[position].commentCount.toString()
            Glide.with(holder.itemView.context)
                .load(recordList[position].imageUrl)  // recordList[position]에서 imageUrl을 로드
                .into(holder.imageUrl)
            holder.mentorName.text = recordList[position].mentorName
            holder.answer.text = recordList[position].answer
        }
    }
}