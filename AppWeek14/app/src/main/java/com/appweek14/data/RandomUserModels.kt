package com.appweek14.data

data class RandomUserResponse(
    val results: List<RandomUser>
)

data class RandomUser(
    val gender: String,
    val email: String,
    val name: RandomUserName,
    val picture: RandomUserPicture
)

data class RandomUserName(
    val title: String,
    val first: String,
    val last: String
)

data class RandomUserPicture(
    val large: String,
    val medium: String,
    val thumbnail: String
)