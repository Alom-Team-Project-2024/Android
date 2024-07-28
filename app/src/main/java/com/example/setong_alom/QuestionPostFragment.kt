package com.example.setong_alom

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class QuestionPostFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var submitButton: ImageButton

    private val client = OkHttpClient()
    private val gson = Gson()

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
        val userId = "22222222"
        val subject = titleEditText.text.toString()
        val content = contentEditText.text.toString()
        val image = "URL" // 실제 이미지 URL로

        //json 객체 생성, 데이터 추가
        val jsonObject = JsonObject().apply {
            addProperty("userId", userId)
            addProperty("subject", subject)
            addProperty("content", content)
            addProperty("image", image)
        }

        //requestBody 생성
        val requestBody = gson.toJson(jsonObject)
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        //request 객체 생성, 서버 URL 설정
        val request = Request.Builder()
            .url("https://your-server-url.com/api/posts") // 실제 서버 URL
            .post(requestBody)
            .build()

        //비동기적으로 요청을 서버에 보냄
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("POST_REQUEST", "Failed to send request", e)
                activity?.runOnUiThread {
                    Toast.makeText(context, "Request failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Post submitted successfully", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Log.e("POST_REQUEST", "Error: ${response.code}")
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Request failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment QuestionPost.
         */
        // TODO: Rename and change types and number of parameters
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