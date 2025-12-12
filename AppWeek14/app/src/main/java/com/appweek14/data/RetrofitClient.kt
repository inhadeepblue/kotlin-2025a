package com.appweek14.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Random User API
    private const val RANDOM_USER_BASE_URL = "https://randomuser.me/"

    // JSONPlaceholder (고정 데이터용) - 필요 시 같이 둘 수도 있음
    private const val JSON_PLACEHOLDER_BASE_URL = "https://jsonplaceholder.typicode.com/"

    // RandomUser Retrofit
    private val randomUserRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(RANDOM_USER_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val randomUserApi: RandomUserApi =
        randomUserRetrofit.create(RandomUserApi::class.java)
}