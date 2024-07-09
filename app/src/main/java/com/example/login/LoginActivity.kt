package com.example.login

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.login.databinding.ActivityLoginBinding
import com.example.login.databinding.ActivityMainBinding
import com.example.login.databinding.RadialMenuBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var radialMenuBinding: RadialMenuBinding
    private var isMenuOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.joinQuestionTextView.setOnClickListener{
            // JoinActivity로 전환
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }


        /**
         * 아래 코드는 하단 네비게이션 테스트를 위한 코드입니다.
         * LoginActivity와는 무관한 코드입니다.
         */

        val radialMenuView = findViewById<View>(R.id.radialMenu)
        radialMenuBinding = RadialMenuBinding.bind(radialMenuView)

        binding.fabMain.setOnClickListener {
            if (isMenuOpen) {
                closeRadialMenu()
            } else {
                openRadialMenu()
            }
        }

        radialMenuBinding.radialBackground.setOnClickListener {
            closeRadialMenu()
        }

        // 각 ImageButton의 클릭 리스너 설정 (홈, MY, 채팅, 게시판)
        radialMenuBinding.fabHome.setOnClickListener {
            // 홈 버튼 클릭 시 처리할 코드
        }

        radialMenuBinding.fabMy.setOnClickListener {
            // MY 버튼 클릭 시 처리할 코드
        }

        radialMenuBinding.fabChat.setOnClickListener {
            // 채팅 버튼 클릭 시 처리할 코드
        }

        radialMenuBinding.fabBoard.setOnClickListener {
            // 게시판 버튼 클릭 시 처리할 코드
        }

    }

    private fun openRadialMenu() {
        binding.backgroundCircle.visibility = View.VISIBLE
        animateButton(binding.fabMain, 3.0f)

        val scaleX = ObjectAnimator.ofFloat(binding.backgroundCircle, "scaleX", 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.backgroundCircle, "scaleY", 1f)
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.duration = 300
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()

        val fabMyAnimator = ObjectAnimator.ofFloat(radialMenuBinding.fabMy, "translationY", -200f)
        val fabChatAnimator = ObjectAnimator.ofFloat(radialMenuBinding.fabChat, "translationX", -200f)
        val fabBoardAnimator = ObjectAnimator.ofFloat(radialMenuBinding.fabBoard, "translationX", 200f)

        val menuAnimatorSet = AnimatorSet()
        menuAnimatorSet.playTogether(fabMyAnimator, fabChatAnimator, fabBoardAnimator)
        menuAnimatorSet.duration = 300
        menuAnimatorSet.start()

        radialMenuBinding.fabMy.visibility = View.VISIBLE
        radialMenuBinding.fabChat.visibility = View.VISIBLE
        radialMenuBinding.fabBoard.visibility = View.VISIBLE

        isMenuOpen = true
    }

    private fun closeRadialMenu() {
        animateButton(binding.fabMain, 1f)

        val scaleX = ObjectAnimator.ofFloat(binding.backgroundCircle, "scaleX", 0f)
        val scaleY = ObjectAnimator.ofFloat(binding.backgroundCircle, "scaleY", 0f)
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.duration = 300
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                binding.backgroundCircle.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animatorSet.start()

        val fabMyAnimator = ObjectAnimator.ofFloat(radialMenuBinding.fabMy, "translationY", 0f)
        val fabChatAnimator = ObjectAnimator.ofFloat(radialMenuBinding.fabChat, "translationX", 0f)
        val fabBoardAnimator = ObjectAnimator.ofFloat(radialMenuBinding.fabBoard, "translationX", 0f)

        val menuAnimatorSet = AnimatorSet()
        menuAnimatorSet.playTogether(fabMyAnimator, fabChatAnimator, fabBoardAnimator)
        menuAnimatorSet.duration = 300
        menuAnimatorSet.start()

        menuAnimatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                radialMenuBinding.fabMy.visibility = View.GONE
                radialMenuBinding.fabChat.visibility = View.GONE
                radialMenuBinding.fabBoard.visibility = View.GONE
                radialMenuBinding.root.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        isMenuOpen = false
    }

    private fun animateButton(view: View, scale: Float) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", scale)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", scale)
        scaleX.duration = 300
        scaleY.duration = 300

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()
    }



}