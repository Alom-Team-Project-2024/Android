package com.example.alom_team_project.login

object JwtProvider {
    private var token: String? = null

    fun setToken(token: String?) {
        JwtProvider.token = token
    }

    fun getToken(): String? {
        return token
    }
}
