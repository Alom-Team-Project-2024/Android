package com.example.alom_team_project

import android.content.Context.MODE_PRIVATE
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val SEJONG_BASE_URL = "https://auth.imsejong.com"
    private const val USER_BASE_URL = "http://15.165.213.186:8080"

    private fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            // HTTP 요청과 응답을 로깅하는 인터셉터 추가
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            // JWT 토큰을 헤더에 추가하는 인터셉터 추가
            .addInterceptor { chain ->
                // SharedPreferences에서 JWT 토큰을 가져옴
                val sharedPref = MyApp.instance.getSharedPreferences("auth", MODE_PRIVATE)
                val token = sharedPref.getString("jwt_token", null)

                // 요청 빌더 생성
                val requestBuilder = chain.request().newBuilder()
                if (!token.isNullOrEmpty()) {
                    // JWT 토큰이 있으면 Authorization 헤더에 추가
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                // 요청을 계속 진행
                chain.proceed(requestBuilder.build())
            }
            .build()
    }


    val sejongApi: SejongApi by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(SEJONG_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(SejongApi::class.java)
    }

    val userApi: UserApi by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(USER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(UserApi::class.java)
    }

}
