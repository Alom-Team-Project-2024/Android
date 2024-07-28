package com.example.setong_alom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class QuestionBoardActivity : AppCompatActivity() {

    private lateinit var questionRecyclerView: RecyclerView
    private lateinit var questionList: ArrayList<QuestionClass>
    lateinit var nicknameList: Array<String>
    lateinit var postTimeList: Array<String>
    lateinit var subjectNameList: Array<String>
    lateinit var contentList: Array<String>
    lateinit var questionImageList: Array<Int>
    lateinit var likeNumList: Array<Int>
    lateinit var commentNumList: Array<Int>
    lateinit var scrapNumList: Array<Int>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_board)

        nicknameList = arrayOf(
            "이수민"
        )
        postTimeList = arrayOf(
            "1분 전"
        )
        subjectNameList= arrayOf(
            "인공지능과 빅데이터"
        )
        contentList = arrayOf(
            "어쩌구저쩌구"
        )
        questionImageList = arrayOf(
            R.drawable.ex_image
        )
        likeNumList = arrayOf(
            1
        )
        commentNumList = arrayOf(
            2
        )
        scrapNumList = arrayOf(
            3
        )

        questionRecyclerView = findViewById(R.id.QuestionRecyclerView)
        questionRecyclerView.layoutManager = LinearLayoutManager(this)
        questionRecyclerView.setHasFixedSize(true)

        questionList = arrayListOf()
        getData()

        val writeButton: ImageButton = findViewById(R.id.writingButton)
        writeButton.setOnClickListener {
            openPostQuestionFragment()
        }
    }

    private fun getData() {
        for (i in questionImageList.indices) {
            val quesClass = QuestionClass(
                nicknameList[i],
                postTimeList[i],
                subjectNameList[i],
                contentList[i],
                questionImageList[i],
                likeNumList[i],
                commentNumList[i],
                scrapNumList[i]
            )
            questionList.add(quesClass)
        }

        questionRecyclerView.adapter = QuestionAdapterClass(questionList)
    }

    private fun openPostQuestionFragment() {
        val fragment = QuestionPostFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.questionViewPage, fragment)
            .addToBackStack(null) // 뒤로 가기 버튼을 사용해 Fragment를 제거하고 이전 상태로
            .commit()
    }
}