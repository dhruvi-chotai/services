package com.example.services_demo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.Serializable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ForegroundService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "ForegroundServiceChannel"
    }

    private lateinit var executorService: ExecutorService
    private lateinit var apiService: ApiService
    private var callback: ApiResponseCallback? = null

    override fun onCreate() {
        Log.d("ForegroundService", "onCreate: ")
        super.onCreate()
//        schedule commands to run after a given delay, or to execute periodically.
        executorService = Executors.newSingleThreadExecutor()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ForegroundService", "onStartCommand: ")
        executorService.execute { fetchDataFromApi() }
        return START_STICKY
    }

    private fun fetchDataFromApi() {
        Log.d("ForegroundService", "fetchDataFromApi: ")
        val retrofit = Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient())
            .build()

        apiService = retrofit.create(ApiService::class.java)
        val call = apiService.fetchData()

        call.enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                Log.d("ForegroundService", "onResponse: ")
                if (response.isSuccessful) {
                    Log.d("ForegroundService", "API call successful")
                    Log.d("ForegroundService", "Response: $response")
                    val posts = response.body()
                    sendDataToActivity(posts)
                } else {
                    callback?.onFailure("API Error")
                }
                stopForeground(true)
                stopSelf()
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Log.d("ForegroundService", "onFailure: ")
                callback?.onFailure("Network or API Call Failure")
                stopForeground(true)
                stopSelf()
            }
        })
    }

    fun setCallback(callback: ApiResponseCallback) {
        Log.d("ForegroundService", "setCallback: ")
        this.callback = callback
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("ForegroundService", "onBind: ")
        return null
    }

    private fun createNotificationChannel() {
        Log.d("ForegroundService", "createNotificationChannel: ")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
    private fun sendDataToActivity(posts: List<Post>?) {
        val intent = Intent("DATA_FETCHED_ACTION")
        intent.putExtra("posts", ArrayList(posts))
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDestroy() {
        Log.d("ForegroundService", "onDestroy: ")
        super.onDestroy()
        executorService.shutdown()
    }
}
