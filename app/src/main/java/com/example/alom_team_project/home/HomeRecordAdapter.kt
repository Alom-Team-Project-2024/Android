package com.example.alom_team_project.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.alom_team_project.databinding.ItemHomeRecordBinding

class HomeRecordAdapter(
    private val recordList: ArrayList<HomeRecordData>,
    private val onItemClickListener: (Long) -> Unit // 클릭 리스너를 생성자에 추가
) : RecyclerView.Adapter<HomeRecordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecordViewHolder {
        val binding = ItemHomeRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeRecordViewHolder(binding, onItemClickListener) // 클릭 리스너를 전달
    }

    override fun getItemCount(): Int {
        return recordList.size
    }

    override fun onBindViewHolder(holder: HomeRecordViewHolder, position: Int) {
        val item = recordList[position]
        holder.bind(item) // 아이템을 바인딩
    }
}
