package com.example.alom_team_project

import android.app.Application

class MyApp : Application() {

    // 애플리케이션의 전역 인스턴스를 저장할 변수
    companion object {
        lateinit var instance: MyApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        // 애플리케이션 인스턴스를 저장
        instance = this
    }
}