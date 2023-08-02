package com.example.services_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foregroundservice.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DataDisplayActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_display)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PostAdapter()
        recyclerView.adapter = adapter

        val json = intent.getStringExtra("posts_json")
        if (json != null) {
            val gson = Gson()
            val type = object : TypeToken<List<Post>>() {}.type
            val posts = gson.fromJson<List<Post>>(json, type)
            adapter.setPosts(posts)
            adapter.notifyDataSetChanged()
        }
    }
}