package com.example.alom_team_project.home

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.alom_team_project.MainActivity
import com.example.alom_team_project.R
import com.example.alom_team_project.chat.ChatListActivity
import com.example.alom_team_project.job_board.MentorBoardActivity
import com.example.alom_team_project.mypage.MypageActivity
import com.example.alom_team_project.question_board.QuestionBoardActivity

class NavigationFragment : Fragment() {

    private lateinit var imageButtonCenter: ImageButton
    private lateinit var constraintLayoutMenu: ConstraintLayout
    private lateinit var navBackground: FrameLayout
    private lateinit var menu1: LinearLayout
    private lateinit var menu2: LinearLayout
    private lateinit var menu3: LinearLayout
    private lateinit var menu4: LinearLayout
    private lateinit var menu5: LinearLayout
    private lateinit var menu6: LinearLayout

    private var viewList = mutableListOf<LinearLayout>()
    private var menuTitleList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.nav, container, false)
        findView(view)
        setListener(view)
        return view
    }

    private fun findView(view: View) {
        imageButtonCenter = view.findViewById(R.id.imageButtonCenter)
        constraintLayoutMenu = view.findViewById(R.id.constraintLayoutMenu)
        navBackground = view.findViewById(R.id.nav_background)
        menu1 = view.findViewById(R.id.menu1)
        menu2 = view.findViewById(R.id.menu2)
        menu3 = view.findViewById(R.id.menu3)
        menu4 = view.findViewById(R.id.menu4)
        menu5 = view.findViewById(R.id.menu5)
        menu6 = view.findViewById(R.id.menu6)

        // 메뉴 리스트에 추가
        viewList.add(menu1)
        viewList.add(menu2)
        viewList.add(menu3)
        viewList.add(menu4)
        viewList.add(menu5)
        viewList.add(menu6)

        // 메뉴 제목 리스트에 추가
        menuTitleList.add("홈")
        menuTitleList.add("구인")
        menuTitleList.add("질문")
        menuTitleList.add("my")
        menuTitleList.add("채팅")
        menuTitleList.add("알림")
    }

    private fun setListener(view: View) {
        imageButtonCenter.setOnClickListener {
            toggleCircleMenu()
        }

        val listener = createTouchListener()
        constraintLayoutMenu.setOnTouchListener(listener)

        for (menuView in viewList) {
            menuView.setOnTouchListener(listener)
        }

        // View의 루트 레이아웃에 터치 리스너를 추가하여 외부 터치 이벤트를 감지
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (constraintLayoutMenu.visibility == View.VISIBLE && !isTouchInsideView(event, constraintLayoutMenu)) {
                    toggleCircleMenu()
                }
            }
            false
        }
    }

    private fun isTouchInsideView(event: MotionEvent, view: View): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        return event.rawX >= x && event.rawX <= (x + view.width) &&
                event.rawY >= y && event.rawY <= (y + view.height)
    }

    private fun createTouchListener(): View.OnTouchListener {
        return object : View.OnTouchListener {
            var x: Float = 0f
            var isTouched = false

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        event.let {
                            x = it.x
                        }

                        return if (v is LinearLayout || v is ConstraintLayout) {
                            isTouched = true
                            true
                        } else {
                            isTouched = false
                            false
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val result = event.x - x
                        when {
                            result > 100 -> {
                                for (linearLayout in viewList) {
                                    startRotate(linearLayout, false)
                                }
                            }
                            result < -100 -> {
                                for (linearLayout in viewList) {
                                    startRotate(linearLayout, true)
                                }
                            }
                            else -> {
                                if (v is LinearLayout) {
                                    toggleClick(v)
                                }
                            }
                        }
                        return false
                    }
                    else -> {
                        return false
                    }
                }
                return false
            }
        }
    }

    private fun toggleClick(currentView: LinearLayout) {
        for (i in viewList.indices) {
            if (currentView.id == viewList[i].id) {
                Toast.makeText(activity, "click menu ${menuTitleList[i]}", Toast.LENGTH_SHORT).show()

                when (menuTitleList[i]) {
                    "질문" -> startActivity(Intent(activity, QuestionBoardActivity::class.java))
                    "홈" -> startActivity(Intent(activity, MainActivity::class.java))
                    "my" -> startActivity(Intent(activity, MypageActivity::class.java))
                    "채팅" -> startActivity(Intent(activity, ChatListActivity::class.java))
                    "구인" -> startActivity(Intent(activity, MentorBoardActivity::class.java))
                }
            }
        }
    }

    private fun startRotate(currentView: LinearLayout, isLeft: Boolean) {
        val layoutparams = currentView.layoutParams as ConstraintLayout.LayoutParams
        val currentAngle = layoutparams.circleAngle

        val targetAngle = currentAngle + if (isLeft) {
            -60
        } else {
            60
        }

        val angleAnimator = ValueAnimator.ofFloat(currentAngle, targetAngle)
        angleAnimator.addUpdateListener {
            layoutparams.circleAngle = it.animatedValue as Float
            currentView.layoutParams = layoutparams
        }

        angleAnimator.duration = 400
        angleAnimator.interpolator = AnticipateOvershootInterpolator()
        angleAnimator.start()
    }

    private fun toggleCircleMenu() {
        val imageButton = view?.findViewById<ImageButton>(R.id.imageButtonCenter)
        imageButton?.let {
            val params = it.layoutParams as FrameLayout.LayoutParams

            if (constraintLayoutMenu.visibility == View.VISIBLE) {
                constraintLayoutMenu.visibility = View.GONE
                navBackground.setBackgroundColor(Color.TRANSPARENT)  // 배경을 투명하게 설정

                // 이미지 버튼 크기 축소
                params.width = dpToPx(50)
                params.height = dpToPx(50)
            } else {
                constraintLayoutMenu.visibility = View.VISIBLE
                navBackground.setBackgroundColor(Color.parseColor("#4D5F5F5F"))  // 70% 투명도 회색

                // 이미지 버튼 크기 확대
                params.width = dpToPx(102)
                params.height = dpToPx(102)
            }

            it.layoutParams = params
            it.requestLayout()  // 변경 사항 적용
        }
    }

    // dp를 px로 변환하는 함수
    fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
