package com.example.setong_alom

import QuestionPostService
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class QuestionPostFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var submitButton: ImageButton

    private val service = RetrofitClient.instance.create(QuestionPostService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_question_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleEditText = view.findViewById(R.id.titleEditText)
        contentEditText = view.findViewById(R.id.contentEditText)
        submitButton = view.findViewById(R.id.submitButton)

        submitButton.setOnClickListener {
            sendPostRequest()
        }
    }

    private fun sendPostRequest() {
        val token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6IjIyMDExMzE1Iiwicm9sZSI6IlVTRVIiLCJuaWNrbmFtZSI6InVzZXIyIiwiaWF0IjoxNzIyNjYyODE2LCJleHAiOjE3MjI2NjQwMTZ9.DB5BO63wGeXtXes2k3tehRM47x6yArr7eRvLVfFJGt8" // Bearer 인증을 위한 토큰
        val subject = titleEditText.text.toString()
        val text = contentEditText.text.toString()

        // 제목과 내용을 확인하고 비어 있지 않도록 체크
        if (subject.isBlank() || text.isBlank()) {
            Toast.makeText(context, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonObject = JsonObject().apply {
            addProperty("subject", subject)
            addProperty("text", text)
        }

        service.postQuestion(token, jsonObject).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Post submitted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("POST_REQUEST", "Error: ${response.code()}")
                    Toast.makeText(context, "Request failed with status code: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("POST_REQUEST", "Failed to send request", t)
                Toast.makeText(context, "Request failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            QuestionPostFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
