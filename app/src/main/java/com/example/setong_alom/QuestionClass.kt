package com.example.setong_alom

import android.provider.ContactsContract.CommonDataKinds.Nickname

data class QuestionClass(
    var nickname: String,
    var postTime: String,
    var subjectName: String,
    var content: String,
    var questionImage: Int,
    var likeNum: Int,
    var commentNum: Int,
    var scrapNum: Int
)
