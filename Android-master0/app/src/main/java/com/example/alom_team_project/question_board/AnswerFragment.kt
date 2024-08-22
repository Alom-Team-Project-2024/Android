package com.example.alom_team_project.question_board

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.databinding.FragmentAnswerBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnswerFragment : Fragment() {

    private var _binding: FragmentAnswerBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AnswerAdapterClass
    private lateinit var answerList: ArrayList<Reply>
    private lateinit var answerService: AnswerPostService


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAnswerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrofit을 사용하여 answerService 초기화
        answerService = RetrofitClient.instance.create(AnswerPostService::class.java)

        // Arguments에서 questionId 받기
        val questionId = arguments?.getLong("QUESTION_ID")

        // questionId를 사용하여 데이터 로딩 또는 기타 작업 수행
        Log.d("AnswerFragment", "Question ID: $questionId")

        answerList = arrayListOf()
        adapter = AnswerAdapterClass(answerList)
        binding.answerContainer.adapter = adapter
        binding.answerContainer.layoutManager = LinearLayoutManager(requireContext())


        if (questionId != null) {
            fetchData(questionId)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getJwtToken(): String {
        val sharedPref = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", "") ?: ""
    }

    private fun fetchData(questionId:Long) {
        val token = getJwtToken()
        Log.d("FETCH_DATA", "Fetching data with token: $token")


        answerService.getAnswers("Bearer $token",questionId).enqueue(object :
            Callback<List<Reply>> {
            override fun onResponse(call: Call<List<Reply>>, response: Response<List<Reply>>) {
                if (response.isSuccessful) {
                    Log.d("FETCH_DATA", "Data fetched successfully")

                    response.body()?.let { questions ->
                        answerList.clear()
                        answerList.addAll(questions)
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    Log.e("FETCH_DATA", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(requireContext(), "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Reply>>, t: Throwable) {

                Log.e("FETCH_DATA", "Failed to fetch data", t)
                Toast.makeText(requireContext(), "Failed to fetch data: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
