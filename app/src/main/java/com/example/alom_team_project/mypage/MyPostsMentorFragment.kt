package com.example.alom_team_project.mypage

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.databinding.FragmentScrapMentorBoardBinding
import com.example.alom_team_project.job_board.MentorAdapterClass
import com.example.alom_team_project.job_board.MentorClass
import com.example.alom_team_project.job_board.MentorDetailFragment
import com.example.alom_team_project.job_board.MentorPostService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPostsMentorFragment : Fragment() {

    private lateinit var binding: FragmentScrapMentorBoardBinding
    private lateinit var adapter: MentorAdapterClass
    private lateinit var mentorList: ArrayList<MentorClass>
    private lateinit var mentorService: MentorPostService
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private val refreshInterval: Long = 12000 // 12초

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentScrapMentorBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // mentor 초기화
        mentorList = arrayListOf()

        // mentorService 초기화
        mentorService = RetrofitClient.instance.create(MentorPostService::class.java)

        // RecyclerView 어댑터 및 레이아웃 매니저 설정
        setupRecyclerView()

        // 데이터 초기 로딩
        fetchData()

        setupAutoRefresh()
    }

    private fun setupRecyclerView() {
        adapter = MentorAdapterClass(
            mentorList = mentorList,
            onItemClickListener = { mentorId ->
                // MentorDetailFragment로 이동
                val fragment = MentorDetailFragment().apply {
                    arguments = Bundle().apply {
                        putLong("MENTOR_ID", mentorId)
                    }
                }
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.mentorViewPage, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        )

        binding.MentorRecyclerView.adapter = adapter
        binding.MentorRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun getJwtToken(): String {
        val sharedPref = requireActivity().getSharedPreferences("auth", AppCompatActivity.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", "") ?: ""
    }

    private fun getSearchText(): String? {
        return arguments?.getString("SEARCH_TEXT")
    }

    private fun getUsername(): String? {
        val sharedPref = requireActivity().getSharedPreferences("auth", MODE_PRIVATE)
        return sharedPref.getString("username", null)
    }

    private fun fetchData() {
        val token = getJwtToken()
        val username = getUsername() ?: ""
        RetrofitClient.userApi.getMyPostsMentorInfo(username,"Bearer $token").enqueue(object : Callback<List<MentorClass>> {
            override fun onResponse(call: Call<List<MentorClass>>, response: Response<List<MentorClass>>) {
                if (response.isSuccessful) {
                    response.body()?.let { mentors ->
                        mentorList.clear()
                        mentorList.addAll(mentors)
                        adapter.filter(getSearchText() ?: "")  // 현재 검색어로 필터링
                    }
                } else {
                    Log.e("FETCH_DATA", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(requireContext(), "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<MentorClass>>, t: Throwable) {
                Log.e("FETCH_DATA", "Failed to fetch data", t)
                Toast.makeText(requireContext(), "Failed to fetch data: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupAutoRefresh() {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                fetchData()
                handler.postDelayed(this, refreshInterval)
            }
        }
        handler.post(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable) // Fragment가 파괴될 때 Runnable 제거
    }
}
