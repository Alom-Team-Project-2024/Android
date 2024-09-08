package com.example.alom_team_project.question_board

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.alom_team_project.databinding.QuestionImageItemBinding

class QuestionImageAdapter(private val images: List<ImageData>) : RecyclerView.Adapter<QuestionImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(val binding: QuestionImageItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = QuestionImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = images[position].imageUrl
        val cardView = holder.binding.cardView
        val imageView = holder.binding.imageView
        // 디스플레이 밀도 가져오기 (dp -> px 변환을 위해)
        val density = holder.itemView.context.resources.displayMetrics.density
        val fullImageUrl = "http://15.165.213.186/uploads/" + imageUrl

        // 이미지가 하나일 때와 여러 개일 때 크기 설정
        if (images.size == 1) {
            // 이미지가 하나일 경우: 큰 크기 설정 (dp -> px 변환)
            val cardWidth = (375 * density).toInt()
            val cardHeight = (222 * density).toInt()

            // 부모 레이아웃이 ConstraintLayout일 경우
            val cardParams = cardView.layoutParams as ViewGroup.LayoutParams
            cardParams.width = cardWidth
            cardParams.height = cardHeight
            cardView.layoutParams = cardParams

            val imageParams = imageView.layoutParams as ViewGroup.LayoutParams
            imageParams.width = cardWidth
            imageParams.height = cardHeight
            imageView.layoutParams = imageParams
        } else {
            // 여러 개일 경우: 작은 크기 설정 (dp -> px 변환)
            val cardWidth = (324 * density).toInt()
            val cardHeight = (222 * density).toInt()

            val cardParams = cardView.layoutParams as ViewGroup.LayoutParams
            cardParams.width = cardWidth
            cardParams.height = cardHeight
            cardView.layoutParams = cardParams

            val imageParams = imageView.layoutParams as ViewGroup.LayoutParams
            imageParams.width = cardWidth
            imageParams.height = cardHeight
            imageView.layoutParams = imageParams
        }

        // Glide를 사용하여 이미지를 로드하고 ImageView에 표시
        Glide.with(holder.binding.imageView.context)
            .load(fullImageUrl)
            .transform(RoundedCorners(70))
            .into(holder.binding.imageView)

        // 레이아웃 변경 사항 적용
        holder.binding.cardView.requestLayout()
    }

    override fun getItemCount(): Int {
        return images.size
    }
}
