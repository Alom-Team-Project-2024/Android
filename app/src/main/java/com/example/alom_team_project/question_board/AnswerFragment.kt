package com.example.alom_team_project.question_board


import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.databinding.FragmentAnswerBinding
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnswerFragment : Fragment() {

    private var _binding: FragmentAnswerBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AnswerAdapterClass
    private lateinit var answerList: ArrayList<Reply>
    private lateinit var answerService: AnswerPostService
    private var isLiked = false
    private var isScrapped = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAnswerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로가기 버튼 설정
        val btnBack: ImageButton = view.findViewById(R.id.back_button)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Retrofit 초기화
        answerService = RetrofitClient.instance.create(AnswerPostService::class.java)

        // Arguments에서 questionId 받기
        val questionId = arguments?.getLong("QUESTION_ID")
        Log.d("AnswerFragment", "Question ID: $questionId")

        // 좋아요 및 스크랩 상태 초기화
        if (questionId != null) {
            isLiked = getLikeStatus(questionId)
            isScrapped = getScrapStatus(questionId)
            updateLikeButtonUI(isLiked)
            updateScrapButtonUI(isScrapped)
        }

        // 좋아요 버튼 클릭 이벤트
        binding.likeButton.setOnClickListener {
            questionId?.let { id ->
                if (!isLiked) {
                    postLike(id)
                } else {
                    Toast.makeText(requireContext(), "이미 좋아요를 눌렀습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 스크랩 버튼 클릭 이벤트
        binding.scrapButton.setOnClickListener {
            val username = getUsername()
            questionId?.let { id ->
                if (username != null && !isScrapped) {
                    postScrap(username, id)
                } else {
                    Toast.makeText(requireContext(), "이미 스크랩을 완료했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // RecyclerView 초기화
        answerList = arrayListOf()
        adapter = AnswerAdapterClass(answerList)
        binding.answerContainer.adapter = adapter
        binding.answerContainer.layoutManager = LinearLayoutManager(requireContext())

        // 질문 정보 및 답변 가져오기
        if (questionId != null) {
            fetchReply(questionId)
            fetchQuestionDetails(questionId)
        }

        binding.sendBtn.setOnClickListener {
            if (questionId != null) {
                sendPostAnswer(questionId)
            }
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

    private fun getUsername(): String? {
        val sharedPref = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPref.getString("username", null)
    }

    private fun sendPostAnswer(questionId: Long) {
        val token = getJwtToken()
        val text = binding.etMessage.text.toString() // 서버 키가 text임

        // 내용을 확인하고 비어 있지 않도록 체크
        if (text.isBlank()) {
            Toast.makeText(context, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonObject = JsonObject().apply {
            addProperty("text", text)
        }

        answerService.postAnswer("Bearer $token", questionId,jsonObject).enqueue(object : Callback<Long> {
            override fun onResponse(call: Call<Long>, response: Response<Long>) {
                if (response.isSuccessful) {
                    var answerId = response.body()!!

                    // 이미지 업로드는 포스트 ID를 받은 후 처리
//                    selectedImageUri?.let { uri ->
//                        uploadImage(postId ?: return)
//                    }
                } else {
                    Log.e("POST_REQUEST", "Error: ${response.code()}")
                    Toast.makeText(context, "Request failed with status code: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Long>, t: Throwable) {
                Log.e("POST_REQUEST", "Failed to send request", t)
                Toast.makeText(context, "Request failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchReply(questionId: Long) {
        val token = getJwtToken()
        Log.d("FETCH_DATA", "Fetching data with token: $token")

        answerService.getAnswers("Bearer $token", questionId).enqueue(object :
            Callback<List<Reply>> {
            override fun onResponse(call: Call<List<Reply>>, response: Response<List<Reply>>) {
                if (response.isSuccessful) {
                    Log.d("FETCH_DATA", "Data fetched successfully")

                    response.body()?.let { replies ->
                        Log.d("FETCH_DATA", "Replies size: ${replies.size}")
                        answerList.clear()
                        answerList.addAll(replies)
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

    private fun fetchQuestionDetails(questionId: Long) {
        val token = getJwtToken()

        answerService.getQuestionFromId("Bearer $token", questionId).enqueue(object : Callback<QuestionClass> {
            override fun onResponse(call: Call<QuestionClass>, response: Response<QuestionClass>) {
                if (response.isSuccessful) {
                    response.body()?.let { question ->
                        bindQuestionToViews(question)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load question details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<QuestionClass>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun bindQuestionToViews(question: QuestionClass) {
        // 질문자 이름 및 질문 내용 설정
        binding.questionerName.text = question.writer
        binding.questionText.text = question.text

        // 좋아요 수, 댓글 수, 스크랩 수 설정
        binding.likeNum.text = question.likes.toString()
        binding.commentNum.text = question.replyCount.toString()
        binding.scrapNum.text = question.scrapCount.toString()
    }

    private fun postLike(postId: Long) {
        val token = getJwtToken()

        answerService.likePost("Bearer $token", postId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    isLiked = true
                    saveLikeStatus(postId, true)
                    updateLikeButtonUI(isLiked)
                    Toast.makeText(requireContext(), "좋아요를 눌렀습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "좋아요를 누르는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun postScrap(username: String, postId: Long) {
        val token = getJwtToken()

        answerService.scrapPost("Bearer $token", username, postId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    isScrapped = true
                    saveScrapStatus(postId, true)
                    updateScrapButtonUI(isScrapped)
                    Toast.makeText(requireContext(), "스크랩을 완료했습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "스크랩을 완료하는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateLikeButtonUI(isLiked: Boolean) {
        if (isLiked) {
            binding.likeButton.setImageResource(R.drawable.like_button2)  // 좋아요된 상태의 이미지
            binding.likeButton.isEnabled = false  // 좋아요 버튼 비활성화
        } else {
            binding.likeButton.setImageResource(R.drawable.like_button)  // 기본 좋아요 이미지
            binding.likeButton.isEnabled = true  // 좋아요 버튼 활성화
        }
    }

    private fun updateScrapButtonUI(isScrapped: Boolean) {
        if (isScrapped) {
            binding.scrapButton.setImageResource(R.drawable.scrap_button2)  // 스크랩된 상태의 이미지
            binding.scrapButton.isEnabled = false  // 스크랩 버튼 비활성화
        } else {
            binding.scrapButton.setImageResource(R.drawable.scrap_button)  // 기본 스크랩 이미지
            binding.scrapButton.isEnabled = true  // 스크랩 버튼 활성화
        }
    }

    private fun saveLikeStatus(postId: Long, isLiked: Boolean) {
        val sharedPref = requireContext().getSharedPreferences("likes", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("post_$postId", isLiked)
            apply()
        }
    }

    private fun saveScrapStatus(postId: Long, isScrapped: Boolean) {
        val sharedPref = requireContext().getSharedPreferences("scraps", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("post_$postId", isScrapped)
            apply()
        }
    }

    private fun getLikeStatus(postId: Long): Boolean {
        val sharedPref = requireContext().getSharedPreferences("likes", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("post_$postId", false)
    }

    private fun getScrapStatus(postId: Long): Boolean {
        val sharedPref = requireContext().getSharedPreferences("scraps", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("post_$postId", false)
    }
}
