package com.example.alom_team_project.question_board.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import com.example.alom_team_project.databinding.PostConfirmDialogBinding

class CustomDialogPost(context: Context): Dialog(context) {
    private lateinit var itemClickListener: ItemClickListener
    private lateinit var binding: PostConfirmDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PostConfirmDialogBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // 배경을 투명하게 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 다이얼로그 바깥쪽 클릭시 닫기
        setCanceledOnTouchOutside(true)

        // 취소 가능 여부
        setCancelable(true)

        binding.noBtn.setOnClickListener {
            dismiss() // 다이얼로그 닫기
        }

        binding.yesBtn.setOnClickListener {
            dismiss()
            itemClickListener.onClick("yes")  // 콜백 호출
            PostCompleteDialog(context).show()
        }

        binding.xBtn.setOnClickListener {
            dismiss()
        }
    }


    // dp를 px로 변환하는 메서드
    private fun dpToPx(dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    interface ItemClickListener {
        fun onClick(message: String)
    }

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }
}