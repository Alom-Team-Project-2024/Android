package com.example.setong_alom

import android.animation.ValueAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import java.time.OffsetDateTime

class MainActivity : AppCompatActivity() {

    private lateinit var imageButtonCenter: ImageButton

    private lateinit var constraintLayoutMenu: ConstraintLayout
    private lateinit var menu1:LinearLayout
    private lateinit var menu2:LinearLayout
    private lateinit var menu3:LinearLayout
    private lateinit var menu4:LinearLayout
    private lateinit var menu5:LinearLayout
    private lateinit var menu6:LinearLayout

    private var viewList = mutableListOf<LinearLayout>()
    private var menuTitleList = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nav)

        //메뉴 제목 리스트에 추가
        menuTitleList.add("home")
        menuTitleList.add("home")
        menuTitleList.add("home")
        menuTitleList.add("home")
        menuTitleList.add("home")
        menuTitleList.add("home")


        findView()
        setListener()
    }

    private fun findView(){
        imageButtonCenter = findViewById(R.id.imageButtonCenter)

        constraintLayoutMenu = findViewById(R.id.constraintLayoutMenu)
        menu1 = findViewById(R.id.menu1)
        menu2 = findViewById(R.id.menu2)
        menu3 = findViewById(R.id.menu3)
        menu4 = findViewById(R.id.menu4)
        menu5 = findViewById(R.id.menu5)
        menu6 = findViewById(R.id.menu6)

        //메뉴 리스트에 추가
        viewList.add(menu1)
        viewList.add(menu2)
        viewList.add(menu3)
        viewList.add(menu4)
        viewList.add(menu5)
        viewList.add(menu6)
    }

    private fun setListener(){
        imageButtonCenter.setOnClickListener{
            toggleCircleMenu()
        }

        //터치 리스너 설정
        val listener = createTouchListener()
        constraintLayoutMenu.setOnTouchListener(listener)

        for(view in viewList){
            view.setOnTouchListener(listener)
        }
    }

    //터치 리스너 생성
    private fun createTouchListener(): View.OnTouchListener {
        return object: View.OnTouchListener{

            //현재 손가락의 x좌표 저장
            var x: Float = 0f
            //사용자가 현재 화면을 터치하는 중인지 나타냄
            var isTouched = false

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                //event가 null이 아닐 때 action 속성으로 종류 판별
                when(event?.action){

                    MotionEvent.ACTION_DOWN->{
                        event.let{
                            x=it.x //터치 시작 위치 저장
                        }

                        return if(v is LinearLayout || v is ConstraintLayout){
                            isTouched=true
                            true
                        }
                        else{
                            isTouched = false
                            false
                        }
                    }

                    MotionEvent.ACTION_MOVE->{
                        return true
                    }

                    MotionEvent.ACTION_UP -> {

                        val result = event.x - x //이동 거리 계산

                        when {

                            result > 100 -> {
                                Log.d("???", "flip right")

                                for(linearLayout in viewList){
                                    startRotate(linearLayout, false)
                                }
                            }

                            result < -100 -> {
                                Log.d("???", "flip left")

                                for(linearLayout in viewList){
                                    startRotate(linearLayout, true)
                                }
                            }

                            else -> {
                                Log.d("???", "just touch")
                            }
                        }
                        return false
                    }
                    else -> {
                        return false

                        if(v is LinearLayout) {
                            toggleClick(v)
                        }
                    }
                }
                return false
            }

        }
    }

    private fun toggleClick(currentView: LinearLayout){
        for(i in 0 until viewList.size){
            if(currentView.id == viewList[i].id){
                Toast.makeText(this,"click menu ${menuTitleList[i]}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startRotate(currentView: LinearLayout, isLeft: Boolean){
        val layoutparams = currentView.layoutParams as ConstraintLayout.LayoutParams
        val currentAngle = layoutparams.circleAngle

        val targetAngle = currentAngle + if(isLeft){
            -60
        }
        else{
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

    //가운데 버튼 클릭 시 가시성 여부
    private fun toggleCircleMenu(){

        if(constraintLayoutMenu.visibility == View.VISIBLE) {
            constraintLayoutMenu.visibility = View.GONE
        }
        else{
            constraintLayoutMenu.visibility = View.VISIBLE
        }
    }
}