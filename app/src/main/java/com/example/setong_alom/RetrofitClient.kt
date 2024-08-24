package com.example.setong_alom

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

object RetrofitClient {

    private const val BASE_URL = "http://15.165.213.186:8080/"

    val instance: Retrofit by lazy {
        val gson: Gson = GsonBuilder()
            .create()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
