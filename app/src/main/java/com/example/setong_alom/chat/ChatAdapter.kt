package com.example.setong_alom.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.setong_alom.databinding.MychatSampleBinding
import com.example.setong_alom.databinding.YourchatSampleBinding

class ChatAdapter(val chattingList: ArrayList<ChatData>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType==0) {
            val binding = YourchatSampleBinding.inflate(LayoutInflater.from(parent.context),parent, false)
            YourChatViewHolder(binding)
        }
        else {
            val binding = MychatSampleBinding.inflate(LayoutInflater.from(parent.context),parent, false)
            MyChatViewHolder(binding)
        }
    }

    override fun getItemCount(): Int {
        return chattingList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is YourChatViewHolder) {
            holder.chat.text = chattingList[position].chat
        }
        else if (holder is MyChatViewHolder) {
            holder.chat.text = chattingList[position].chat
        }
    }

    override fun getItemViewType(position: Int): Int {
        return chattingList[position].viewType
    }

}