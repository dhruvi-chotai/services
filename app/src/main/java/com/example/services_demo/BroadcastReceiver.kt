package com.example.services_demo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("Broadcast Receiver", "onReceive: ")
        if (intent?.action == "my_custom_action") {
            val message = intent.getStringExtra("message")
            Log.d("BroadcastReceiver", "$message")

            Toast.makeText(context, "Received: $message", Toast.LENGTH_SHORT).show()
        }
    }
}