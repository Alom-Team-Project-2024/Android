package com.example.setong_alom

import android.content.Context
import android.content.SharedPreferences

object TokenManager {

    private const val PREFS_NAME = "app_prefs"
    private const val TOKEN_KEY = "auth_token"

    // 토큰 저장
    fun saveToken(context: Context, token: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(TOKEN_KEY, token)
            apply()
        }
    }

    // 토큰 불러오기
    fun getToken(context: Context): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(TOKEN_KEY, null)
    }

    // 토큰 삭제
    fun clearToken(context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove(TOKEN_KEY)
            apply()
        }
    }
}