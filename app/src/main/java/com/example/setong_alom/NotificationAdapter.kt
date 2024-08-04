package com.example.setong_alom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.setong_alom.databinding.ItemNotificationBinding

class NotificationAdapter(val notificationList: ArrayList<NotificationData>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 클릭 리스너 인터페이스 정의
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var listener: OnItemClickListener? = null

    // 클릭 리스너 설정 메서드
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = notificationList[position]
        if (holder is NotificationViewHolder) {
            holder.bind(currentItem)
        }
    }

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding):RecyclerView.ViewHolder(binding.root) {
        val content = binding.content
        val time = binding.time
        val isRead = binding.unreadIcon

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(position)
                }
            }
        }

        fun bind(item: NotificationData) {
            content.text = item.content
            time.text = item.time
            isRead.setImageResource(
                if (item.isRead) R.drawable.transparent // 읽은 경우 투명 이미지
                else R.drawable.circle_img // 읽지 않은 경우 원래 이미지
            )
        }
    }

}