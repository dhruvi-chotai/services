package com.example.services_demo

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.foregroundservice.R
import com.google.gson.Gson


class MainActivity : AppCompatActivity(), ApiResponseCallback {

    private var foregroundService: ForegroundService? = null
    private lateinit var background_btn: Button
    private lateinit var foreground_btn: Button
    private lateinit var bound_btn: Button
    private lateinit var localbroadcastReceiver: BroadcastReceiver
    private var boundService: BoundService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            Log.d("MainActivity", "onServiceConnected: ")
            val localBinder = binder as BoundService.LocalBinder
            boundService = localBinder.getService()
            isBound = true
            boundService?.setCallback(this@MainActivity)
            boundService?.fetchDataFromApi()
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            Log.d("MainActivity", "onServiceDisconnected: ")
            isBound = false
        }
    }

    private val myReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "my_custom_action") {
                val message = intent.getStringExtra("message")
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "onCreate: ")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bound_btn = findViewById(R.id.bound_btn)
        bound_btn.setOnClickListener {
            startBoundService()
        }

        background_btn = findViewById(R.id.background_btn)
        background_btn.setOnClickListener {
            startBackgroundService()
        }

        foreground_btn = findViewById(R.id.foreground_btn)
        foreground_btn.setOnClickListener {
            foregroundService = ForegroundService()
            foregroundService?.setCallback(this)
            startForegroundService()
        }

        localbroadcastReceiver = MyBroadcastReceiver()
    }

    private val localBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("Broadcast Receiver", "onReceive: ")
            if (intent?.action == "DATA_FETCHED_ACTION") {
                val posts = intent.getSerializableExtra("posts") as? List<Post>
                onDataFetched(posts)
            }
        }
    }

    override fun onResume() {
        Log.d("Broadcast Receiver", "onResume: ")
        super.onResume()

        // Register the BroadcastReceiver to listen for data from ForegroundService
        val intentFilter = IntentFilter("DATA_FETCHED_ACTION")
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver,
            intentFilter)
    }

    override fun onPause() {
        Log.d("Broadcast Receiver", "onPause: ")
        super.onPause()

        // Unregister the BroadcastReceiver to avoid leaks
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastReceiver)
    }


    private fun startBackgroundService() {
        Log.d("startBackgroundService", "startBackgroundService: ")
        val intent = Intent(this, BackgroundService::class.java)
        startService(intent)
    }

    private fun startForegroundService() {
        Log.d("startForegroundService", "startForegroundService: ")
        val serviceIntent = Intent(this, ForegroundService::class.java)
        startService(serviceIntent)
    }

    private fun startBoundService() {
        Log.d("startBoundService", "startBoundService: ")
        val serviceIntent = Intent(this, BoundService::class.java)
        if (!isBound) {
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        } else {
            boundService?.fetchDataFromApi()
        }
    }

    override fun onDataFetched(posts: List<Post>?) {
        if (posts != null) {
            Log.d("MainActivity", "onDataFetched: Received data in MainActivity")
            val intent = Intent(this, DataDisplayActivity::class.java)
            val gson = Gson()
            val json = gson.toJson(posts)
            intent.putExtra("posts_json", json)
            startActivity(intent)
        } else {
            Log.d("MainActivity", "onDataFetched: Failed to fetch data in MainActivity")
            Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroy() {
        Log.d("MainActivity", "onDestroy: ")
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    override fun onSuccess(response: Post) {
        Log.d("MainActivity", "onSuccess: ")
        // Handle successful response here
        Toast.makeText(this, "API call successful", Toast.LENGTH_SHORT).show()
    }

    override fun onFailure(errorMessage: String) {
        Log.d("MainActivity", "onFailure: ")
        // Handle failure/error here
        Toast.makeText(this, "API call failed: $errorMessage", Toast.LENGTH_SHORT).show()
    }

    val onCallBack = object : ApiResponseCallback {
        override fun onSuccess(response: Post) {
            TODO("Not yet implemented")
        }

        override fun onFailure(errorMessage: String) {
            TODO("Not yet implemented")
        }

        override fun onDataFetched(posts: List<Post>?) {
            TODO("Not yet implemented")
        }

    }
}

