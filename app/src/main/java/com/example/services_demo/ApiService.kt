package com.example.services_demo

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {

    @GET("posts")
    fun fetchData(): Call<List<Post>>
}
