package com.example.alom_team_project.home

import android.content.Context
import android.util.TypedValue
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.alom_team_project.databinding.ItemHomeRecordBinding
import android.view.View
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.alom_team_project.R

class HomeRecordViewHolder(
    private val binding: ItemHomeRecordBinding,
    private val onItemClickListener: (Long) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun dpToPx(context: Context, dp: Int): Int {
        val resources = context.resources
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()
    }
    fun bind(record: HomeRecordData) {
        // 아이템 데이터 바인딩
        binding.title.text = record.title
        binding.commentCount.text = record.commentCount.toString()
        binding.mentorName.text = record.mentorName
        binding.answer.text = record.answer

        val imageWidthPx = dpToPx(itemView.context, 150)
        val imageHeightPx = dpToPx(itemView.context, 100)
        // 이미지 로드
        if (record.images.isNotEmpty()) {
            // 첫 번째 이미지 URL 가져오기
            val imageUrl = record.images[0].imageUrl
            val fullImageUrl = "http://15.165.213.186/uploads/" + imageUrl

            Glide.with(itemView.context)
                .load(fullImageUrl)
                .apply(RequestOptions()
                    .override(imageWidthPx, imageHeightPx) // DP를 PX로 변환하여 크기 조정
                    .transform(RoundedCorners(16)) // 모서리 둥글게 설정 (16px)
                )
                .centerCrop()  // 중앙을 기준으로 자르기
            binding.imageUrl.visibility = View.VISIBLE // 이미지가 있을 때는 ImageView를 보이도록 설정
        } else {
            binding.imageUrl.visibility = View.GONE // 이미지 URL이 없을 때는 ImageView를 숨깁니다.
        }

        // 아이템 클릭 리스너 설정
        itemView.setOnClickListener {
            onItemClickListener(record.id)  // 클릭된 아이템의 ID 전달
        }
    }
}
