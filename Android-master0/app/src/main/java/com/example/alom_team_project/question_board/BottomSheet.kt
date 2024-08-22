package com.example.alom_team_project.question_board


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.alom_team_project.databinding.FragmentBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MyHistoryBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 채팅 나가기 버튼 클릭시
        binding.exitButton.setOnClickListener {
            Toast.makeText(context, "채팅 나가기 버튼 클릭", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        // 신고하기 버튼 클릭시
        binding.reportButton.setOnClickListener {
            Toast.makeText(context, "신고하기 버튼 클릭", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        // 멘토 평가하기 버튼 클릭시
        binding.ratingButton.setOnClickListener {
            Toast.makeText(context, "멘토 평가하기 버튼 클릭", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}