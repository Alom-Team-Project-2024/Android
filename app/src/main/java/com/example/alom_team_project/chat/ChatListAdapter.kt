package com.example.alom_team_project.chat

import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.alom_team_project.R
import com.example.alom_team_project.databinding.ChatListBinding
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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

    @RequiresApi(Build.VERSION_CODES.O)
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


        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: ChatList) {
            val utcTime = item.time
            Log.d("zzzz", utcTime.toString())

            // 기본적으로 빈 문자열을 처리할 경우 "Unknown time"을 설정
            val elapsedTime: String = if (utcTime.isNotEmpty()) {
                // DateTimeFormatter를 커스터마이즈하여 오프셋 없는 포맷을 처리
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

                try {
                    // UTC로 파싱한 뒤, 서울 시간대로 변환
                    val utcDateTime = LocalDateTime.parse(utcTime, formatter).atZone(ZoneId.of("UTC"))
                    val koreaDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"))

                    // 현재 디바이스의 시간을 서울 시간 기준으로 가져오기
                    val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))

                    // 두 시간의 차이를 계산
                    val duration = Duration.between(koreaDateTime, now)

                    // 차이를 원하는 형식으로 변환
                    val days = duration.toDays()
                    val hours = duration.toHours() % 24
                    val minutes = duration.toMinutes() % 60

                    // 차이를 문자열로 출력
                    when {
                        days > 0 -> "$days 일 전"
                        hours > 0 -> "$hours 시간 전"
                        minutes > 0 -> "$minutes 분 전"
                        else -> "방금 전"
                    }
                } catch (e: Exception) {
                    Log.e("ChatListAdapter", "Date parsing error: ${e.message}")
                    "" // 기본값으로 설정
                }
            } else {
                "" // 빈 문자열일 때 기본값
            }

            // 프로필 이미지 설정
            bindProfileImage(profileImageView, item.profile)
            // 나머지 데이터 설정
            username.text = item.name
            content.text = item.content
            time.text = elapsedTime
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

    fun bindProfileImage(imageView: ImageView, profile: String?) {
        profile?.let {
            if (it.startsWith("http")) {
                // URL 기반 이미지 로드 (Glide 사용)
                Glide.with(imageView.context)
                    .load(it)
                    .into(imageView)
            } else {
                // Base64 이미지 로드
                val decodedBytes = Base64.decode(it, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                imageView.setImageBitmap(bitmap)
            }
        } ?: run {
            imageView.setImageResource(R.drawable.profile) // 기본 이미지 설정
        }
    }
}