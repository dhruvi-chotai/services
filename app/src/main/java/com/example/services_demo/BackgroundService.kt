package com.example.services_demo

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class   BackgroundService: Service(){

    companion object{
        private const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    }
    private var callback: ApiResponseCallback? = null
    private lateinit var retrofit: Retrofit
    private lateinit var apiService: ApiService

    override fun onCreate() {
        Log.d("Background", "onCreate: ")
        super.onCreate()

        // Initialize Retrofit
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        // Create the ApiService interface
        apiService = retrofit.create(ApiService::class.java)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Background", "onStartCommand: ")
        // Perform your background task using Retrofit here
        fetchDataFromServer()

        // Return START_STICKY to ensure the service keeps running even if it's killed by the system
        return START_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? {
        Log.d("Background", "onBind: ")
        return null
    }

    private fun fetchDataFromServer() {
        Log.d("Background", "fetchDataFromServer: ")
    // Perform the API call using Retrofit
    // Example API call:
        val call = apiService.fetchData()
        call.enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    // Handle the successful response here
                    Log.d("Background", "API call successful")
                    Log.d("Background", "Response: $response")
                    val posts = response.body()
                    sendDataToActivity(posts)
                } else {
                    callback?.onFailure("API Error")
                }
                // Stop the service when the task is complete
                stopSelf()

            }
            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Log.d("Background", "onFailure: ")
                // Handle the failure here
                // Stop the service when the task is complete
                stopSelf()
            }
        })
    }
    private fun sendDataToActivity(posts: List<Post>?) {
        val intent = Intent("DATA_FETCHED_ACTION")
        intent.putExtra("posts", ArrayList(posts))
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
    override fun onDestroy() {
        Log.d("Background", "onDestroy: ")
        super.onDestroy()
    }
}