package com.example.alom_team_project.question_board


import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.alom_team_project.R
import com.example.alom_team_project.databinding.SubjectItemBinding
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class SubjectAdapter(val subjectList: ArrayList<Subject>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var filteredList: ArrayList<Subject> = ArrayList(subjectList)

    init {
        filteredList = ArrayList(subjectList) // 초기화 시 filteredList에 questionList를 할당
    }
    class SubjectViewHolder(private val binding: SubjectItemBinding):RecyclerView.ViewHolder(binding.root){

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(subject: Subject) {
            binding.subject.text = subject.subject
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val binding = SubjectItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return SubjectViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filteredList.size  // filteredList 사용
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = filteredList[position]  // filteredList 사용
        if (holder is SubjectViewHolder) {
            holder.bind(currentItem)
        }
    }

    fun filter(query: String) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())
        filteredList = if (query.isEmpty()) {
            ArrayList(subjectList)  // 전체 리스트를 복사
        } else {
            subjectList.filter {
                it.subject.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
            } as ArrayList<Subject>
        }
        notifyDataSetChanged()  // 데이터 변경 알림
    }

    fun updateSubjectList(newList: List<Subject>) {
        subjectList.clear()
        subjectList.addAll(newList)
        filter("") // 필터 초기화
    }
}