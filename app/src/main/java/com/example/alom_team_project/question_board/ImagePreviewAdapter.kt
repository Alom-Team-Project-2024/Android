package com.example.alom_team_project.question_board


import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.alom_team_project.databinding.ImageItemBinding


class ImagePreviewAdapter(private val imageUris: List<Uri>) : RecyclerView.Adapter<ImagePreviewAdapter.ImagePreviewHolder>() {

    inner class ImagePreviewHolder(val binding: ImageItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagePreviewHolder {
        val binding = ImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImagePreviewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImagePreviewHolder, position: Int) {
        val uri = imageUris[position]
        holder.binding.imageView.setImageURI(uri)
    }

    override fun getItemCount(): Int = imageUris.size
}
