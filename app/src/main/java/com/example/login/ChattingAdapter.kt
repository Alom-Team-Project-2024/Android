package com.example.login

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.login.databinding.MychattingSampleBinding
import com.example.login.databinding.YourchattingSampleBinding

class ChattingAdapter(val chattingList: ArrayList<ChattingData>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType==0) {
            val binding = YourchattingSampleBinding.inflate(LayoutInflater.from(parent.context),parent, false)
            YourChattingViewHolder(binding)
        }
        else {
            val binding = MychattingSampleBinding.inflate(LayoutInflater.from(parent.context),parent, false)
            MyChattingViewHolder(binding)
        }
    }

    override fun getItemCount(): Int {
        return chattingList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is YourChattingViewHolder) {
            holder.chat.text = chattingList[position].chat
        }
        else if (holder is MyChattingViewHolder) {
            holder.chat.text = chattingList[position].chat
        }
    }

    override fun getItemViewType(position: Int): Int {
        return chattingList[position].viewType
    }

}