package com.example.login.retrofit

import com.example.login.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface ApiService {
    @GET("/users")
    fun getUsers(): Call<List<User>>

    @GET("/users/{id}")
    fun getUser(@Path("id") id: Long): Call<User>

    @FormUrlEncoded
    @POST("/users")
    fun createUser(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<Void>
}