package com.example.setong_alom.chatlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.setong_alom.databinding.ChatListBinding

class ChatListAdapter(private val originalList: ArrayList<ChatList>) : RecyclerView.Adapter<ChatListAdapter.ChattingListViewHolder>() {

    private var filteredList: ArrayList<ChatList> = ArrayList(originalList)

    interface OnItemClickListener {
        fun onItemClick(chatRoomId: Long)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChattingListViewHolder {
        val binding = ChatListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChattingListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: ChattingListViewHolder, position: Int) {
        val currentItem = filteredList[position]
        holder.bind(currentItem)
    }

    inner class ChattingListViewHolder(private val binding: ChatListBinding) : RecyclerView.ViewHolder(binding.root) {
        val profileImageView = binding.profile
        val username = binding.userName
        val content = binding.content
        val time = binding.time

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val chatRoomId = filteredList[position].chatRoomId // chatRoomId 가져오기
                    listener?.onItemClick(chatRoomId) // chatRoomId 전달
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

    // 필터 메소드
    fun filter(query: String) {
        val normalizedQuery = query.replace("\\s".toRegex(), "").lowercase()
        filteredList.clear() // 필터링할 때 리스트를 비웁니다.

        if (normalizedQuery.isEmpty()) {
            filteredList.addAll(originalList) // 원본 리스트를 추가
        } else {
            for (item in originalList) {
                val normalizedItemName = item.name.replace("\\s".toRegex(), "").lowercase()
                if (normalizedItemName.contains(normalizedQuery)) {
                    filteredList.add(item)
                }
            }
        }
        notifyDataSetChanged() // 데이터 변경 알리기
    }
}
