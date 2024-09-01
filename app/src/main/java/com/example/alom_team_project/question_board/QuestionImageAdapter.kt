package com.example.alom_team_project.question_board

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.alom_team_project.databinding.QuestionImageItemBinding

class QuestionImageAdapter(private val images: List<ImageData>) : RecyclerView.Adapter<QuestionImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(val binding: QuestionImageItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = QuestionImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = images[position].imageUrl
        val fullImageUrl = "http://15.165.213.186/uploads/" + imageUrl

        // Glide를 사용하여 이미지를 로드하고 ImageView에 표시
        Glide.with(holder.binding.imageView.context)
            .load(fullImageUrl)
            .into(holder.binding.imageView)
    }

    override fun getItemCount(): Int {
        return images.size
    }
}
