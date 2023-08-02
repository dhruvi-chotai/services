package com.example.services_demo

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BoundService : Service() {

    private val binder = LocalBinder()
    private lateinit var apiService: ApiService
    private var callback: ApiResponseCallback? = null

    inner class LocalBinder : Binder() {
        fun getService(): BoundService = this@BoundService
    }

    override fun onCreate() {
        Log.d("BoundService", "onCreate: ")
        super.onCreate()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("BoundService", "onBind: ")
        return binder
    }

    fun fetchDataFromApi() {
        Log.d("BoundService", "fetchDataFromApi: ")
        val call = apiService.fetchData()

        call.enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    Log.d("BoundService", "API call successful")
                    Log.d("BoundService", "Response: $response")
                    val posts = response.body()
                    callback?.onDataFetched(posts)
                } else {
                    Log.e("BoundService", "onResponse: Failed to fetch data: " +
                            "${response.code()}")
                    callback?.onDataFetched(null)
                }

            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Log.d("BoundService", "onFailure: ")
                callback?.onFailure("Network or API Call Failure")
            }

        })

    }

    fun setCallback(callback: ApiResponseCallback) {
        Log.d("BoundService", "setCallback: ")
        this.callback = callback
    }

}

