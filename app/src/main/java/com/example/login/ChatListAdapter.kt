package com.example.login

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.login.databinding.ChattingListBinding

class ChatListAdapter(private val chattingList: ArrayList<ChatList>) : RecyclerView.Adapter<ChatListAdapter.ChattingListViewHolder>() {

    // 클릭 리스너 인터페이스 정의
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var listener: OnItemClickListener? = null

    // 클릭 리스너 설정 메서드
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

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
        val username = binding.userName
        val content = binding.content
        val time = binding.time

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(position)
                }
            }
        }

        fun bind(item: ChatList) {
            profileImageView.setImageResource(item.profile)
            username.text = item.name
            content.text = item.content
            time.text = item.time
        }
    }
}
