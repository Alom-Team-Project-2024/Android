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
import com.example.setong_alom.databinding.ConfirmDialogBinding

class CustomDialogC(context: Context): Dialog(context) {
    private lateinit var itemClickListener: ItemClickListener
    private lateinit var binding: ConfirmDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ConfirmDialogBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // 배경을 투명하게 설정 (Make the background transparent)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 다이얼로그 바깥쪽 클릭시 닫기 (Dismiss dialog on outside touch)
        setCanceledOnTouchOutside(true)

        // 취소 가능 여부
        setCancelable(true)

        binding.noBtn.setOnClickListener {
            dismiss() // 다이얼로그 닫기
        }

        binding.yesBtn.setOnClickListener {
            dismiss()
        }
    }

    // 다이얼로그 위치 조정 메서드
    private fun setDialogPosition(dpFromTop: Int) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            size.x = metrics.bounds.width()
            size.y = metrics.bounds.height()
        } else {
            @Suppress("DEPRECATION")
            display.getSize(size)
        }

        val layoutParams = window?.attributes
        layoutParams?.gravity = Gravity.TOP
        layoutParams?.y = dpToPx(dpFromTop.toFloat())
        window?.attributes = layoutParams
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