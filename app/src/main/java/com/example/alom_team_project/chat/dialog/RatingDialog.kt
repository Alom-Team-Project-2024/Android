package com.example.alom_team_project.chat.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import com.example.alom_team_project.databinding.AssessDialogBinding

class CustomDialogR(context: Context) : Dialog(context) {
    private lateinit var itemClickListener: ItemClickListener
    private lateinit var binding: AssessDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AssessDialogBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // 배경을 투명하게 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 다이얼로그 바깥쪽 클릭시 닫기
        setCanceledOnTouchOutside(true)

        // 취소 가능 여부
        setCancelable(true)

        binding.buttonX.setOnClickListener {
            dismiss()
        }

        binding.button3.isEnabled = false
        binding.button3.setBackgroundColor(Color.parseColor("#FFFFFF"))

        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            if (rating > 0) {
                binding.button3.isEnabled = true
                binding.button3.setBackgroundColor(Color.parseColor("#BAEDFB"))
            } else {
                binding.button3.isEnabled = false
                binding.button3.setBackgroundColor(Color.WHITE)            }
        }

        binding.button3.setOnClickListener {
            // 별점과 코멘트 서버로 전송
            dismiss()
        }
    }

    interface ItemClickListener {
        fun onClick(message: String)
    }

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }
}