package com.example.login

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.login.databinding.ActivityRetrofitBinding
import com.example.login.model.User
import com.example.login.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RetrofitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRetrofitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRetrofitBinding.inflate(layoutInflater)
        setContentView(binding.root)



        fetchUsers()
        createUser("kotlinUsername111", "kotlinPassword111")

    }


    private fun fetchUsers() {
        val call = RetrofitClient.instance.getUsers()
        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val users = response.body()
                    users?.forEach {
                        Log.d("MainActivity", "User: ${it.username}, Password: ${it.password}")
                    }
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.d("MainActivity", "Error fetching users", t)
            }

        })
    }

    private fun createUser(username: String, password: String) {
        val call = RetrofitClient.instance.createUser(username, password)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("MainActivity", "OK")
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("MainActivity", "Error creating user", t)
            }
        })
    }
}