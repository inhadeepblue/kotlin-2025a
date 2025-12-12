package com.appweek14.data

import retrofit2.http.GET
import retrofit2.http.Query

interface RandomUserApi {

    // https://randomuser.me/api/?results=5
    @GET("api/")
    suspend fun getRandomUsers(
        @Query("results") count: Int = 5
    ): RandomUserResponse
}