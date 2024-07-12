package com.example.login

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.login.databinding.ChattingListBinding

class ChattingListAdapter(val chattingList: ArrayList<ChattingList>) : RecyclerView.Adapter<ChattingListAdapter.ChattingListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChattingListViewHolder {
        val binding = ChattingListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChattingListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return chattingList.size
    }

    override fun onBindViewHolder(holder: ChattingListViewHolder, position: Int) {
        val currentItem = chattingList[position]
        holder.bind(currentItem)
    }

    inner class ChattingListViewHolder(private val binding: ChattingListBinding) : RecyclerView.ViewHolder(binding.root) {
        val profileImageView = binding.profile

        fun bind(item: ChattingList) {
            profileImageView.setImageResource(item.profile)
        }
    }
}