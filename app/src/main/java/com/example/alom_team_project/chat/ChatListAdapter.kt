package com.example.alom_team_project.chat

import android.os.Build
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
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

            // 기본적으로 빈 문자열을 처리할 경우 "Unknown time"을 설정
            val elapsedTime: String = if (utcTime.isNotEmpty() && utcTime != "9999-12-31T23:59:59") {
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
                "" // 메시지와 타임스탬프가 비어있는 경우 표시할 기본 문자열
            }

            // 나머지 데이터 설정
            binding.userName.text = item.name
            binding.content.text = item.content
            binding.time.text = elapsedTime

            bindProfileImage(binding.profile, item.profile)
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
        if (profile != null) {
            val imageUrl = "http://15.165.213.186/$profile"
            Log.d("ChatListAdapter", "Loading image from URL: $imageUrl")

            Glide.with(imageView.context)
                .load(imageUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.profile)
        }
    }



}