package com.example.alom_team_project.job_board

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.databinding.FragmentMentorPostBinding
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MentorPostFragment : Fragment() {


    private var _binding: FragmentMentorPostBinding? = null
    private val binding get() = _binding!!

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var submitButton: ImageButton
    private lateinit var mentorService: MentorPostService

    private val service = RetrofitClient.instance.create(MentorPostService::class.java)

    private var status = "mentor"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMentorPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleEditText = view.findViewById(R.id.titleEditText)
        contentEditText = view.findViewById(R.id.contentEditText)
        submitButton = view.findViewById(R.id.submitButton)


        // mentorService 초기화
        mentorService = RetrofitClient.instance.create(MentorPostService::class.java)




        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 버튼 클릭 시 언더바 전환 및 색상 변경
        binding.btnMentor.setOnClickListener {
            selectButton(binding.btnMentor)
            status = "mentor"
        }

        binding.btnMentee.setOnClickListener {
            selectButton(binding.btnMentee)
            status = "mentee"
        }

        // 초기 선택 상태 설정 (기본적으로 질문 게시판 선택됨)
        selectButton(binding.btnMentor)

        submitButton.setOnClickListener {
            sendPostRequest(status)
        }

        binding.root.setOnClickListener {
            hideKeyboard()
        }
    }

    private fun selectButton(selectedButton: TextView) {
        // 버튼 색상 설정
        val selectedColor = "#1391B4" // 파란색
        val unselectedColor = "#000000" // 기본 검은색

        // 언더바 visibility 조정
        if (selectedButton.id == binding.btnMentor.id) {
            binding.btnMentor.setTextColor(Color.parseColor(selectedColor))
            binding.btnMentee.setTextColor(Color.parseColor(unselectedColor))
            binding.underbar.visibility = android.view.View.VISIBLE
            binding.underbar2.visibility = android.view.View.INVISIBLE
        } else {
            binding.btnMentor.setTextColor(Color.parseColor(unselectedColor))
            binding.btnMentee.setTextColor(Color.parseColor(selectedColor))
            binding.underbar.visibility = android.view.View.INVISIBLE
            binding.underbar2.visibility = android.view.View.VISIBLE
        }
    }

    private fun getJwtToken(): String {
        val sharedPref = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", "") ?: ""
    }

    private fun sendPostRequest(status:String) {
        val token = getJwtToken()
        val title = titleEditText.text.toString()
        val text = contentEditText.text.toString()
        var category: String

        if(status=="mentor"){
            category = "FIND_MENTOR"
        }
        else{
            category = "FIND_MENTEE"
        }

        // 제목과 내용을 확인하고 비어 있지 않도록 체크
        if (title.isBlank() || text.isBlank()) {
            Toast.makeText(context, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonObject = JsonObject().apply {
            addProperty("title", title)
            addProperty("text", text)
            addProperty("category",category)
        }

        service.postMentor("Bearer $token", jsonObject).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "글이 정상적으로 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
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

    // 프래그먼트에서 키보드 숨기기
    private fun hideKeyboard() {
        val inputManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = requireActivity().currentFocus ?: view
        currentFocusedView?.let {
            inputManager.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}