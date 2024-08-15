package com.example.setong_alom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.setong_alom.databinding.QuestionBoardItemBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

class QuestionAdapterClass(private val questionList: ArrayList<QuestionClass>) :
    RecyclerView.Adapter<QuestionAdapterClass.ViewHolder>() {

    private var filteredList: ArrayList<QuestionClass> = ArrayList(questionList)

    init {
        filteredList = ArrayList(questionList) // 초기화 시 filteredList에 questionList를 할당
    }


    class ViewHolder(private val binding: QuestionBoardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(question: QuestionClass) {
            // 날짜 포맷팅
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
            binding.postTime.text = question.createdAt?.let { dateFormat.format(it) } ?: "Unknown"
            binding.subjectName.text = question.subject
            binding.content.text = question.text

            val user = question.user
            if (user != null && !user.profileImage.isNullOrEmpty()) {
                Glide.with(binding.questionImage.context)
                    .load(user.profileImage)
                    .into(binding.questionImage)
                binding.questionImage.visibility = View.VISIBLE
            } else {
                binding.questionImage.visibility = View.GONE
            }

            binding.likeNum.text = question.likes.toString()
            binding.commentNum.text = question.replyCount.toString()
            binding.scrapNum.text = question.clips.toString()

            // Button 스타일 설정 (필요에 따라 조정)
            binding.likeButton.setBackgroundResource(R.drawable.like_button)
            binding.commentButton.setBackgroundResource(R.drawable.comment_button)
            binding.scrapButton.setBackgroundResource(R.drawable.scrap_button)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = QuestionBoardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = filteredList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    fun filter(query: String) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())
        filteredList = if (query.isEmpty()) {
            ArrayList(questionList)  // 전체 리스트를 복사
        } else {
            questionList.filter {
                it.subject.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
            } as ArrayList<QuestionClass>
        }
        notifyDataSetChanged()  // 데이터 변경 알림
    }

}
