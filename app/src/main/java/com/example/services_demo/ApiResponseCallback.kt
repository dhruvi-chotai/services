package com.example.services_demo

interface ApiResponseCallback {
    fun onSuccess(response: Post)
    fun onFailure(errorMessage: String)
    fun onDataFetched(posts: List<Post>?)
}
