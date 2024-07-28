package com.example.setong_alom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QuestionAdapterClass(private val questionList: ArrayList<QuestionClass>) :
    RecyclerView.Adapter<QuestionAdapterClass.ViewHolder>() {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val nickname: TextView = itemView.findViewById(R.id.nickname)
        val postTime: TextView = itemView.findViewById(R.id.post_time)
        val subjectName: TextView = itemView.findViewById(R.id.subjectName)
        val content: TextView = itemView.findViewById(R.id.content)
        val questionImage: ImageView = itemView.findViewById(R.id.question_image)
        val likeButton: ImageButton = itemView.findViewById(R.id.like_button)
        val likeNum: TextView = itemView.findViewById(R.id.likeNum)
        val commentButton: ImageButton = itemView.findViewById(R.id.comment_button)
        val commentNum: TextView = itemView.findViewById(R.id.commentNum)
        val scrapButton: ImageButton = itemView.findViewById(R.id.scrap_button)
        val scrapNum: TextView = itemView.findViewById(R.id.scrapNum)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.question_board_item, parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = questionList[position]
        holder.nickname.text = currentItem.nickname
        holder.postTime.text = currentItem.postTime
        holder.subjectName.text = currentItem.subjectName
        holder.content.text = currentItem.content
        holder.questionImage.setImageResource(currentItem.questionImage)
        holder.likeNum.text = currentItem.likeNum.toString()
        holder.commentNum.text = currentItem.commentNum.toString()
        holder.scrapNum.text = currentItem.scrapNum.toString()

        holder.likeButton.setBackgroundResource(R.drawable.like_button)
        holder.commentButton.setBackgroundResource(R.drawable.comment_button)
        holder.scrapButton.setBackgroundResource(R.drawable.scrap_button)
    }

    override fun getItemCount(): Int {
        return questionList.size
    }
}
