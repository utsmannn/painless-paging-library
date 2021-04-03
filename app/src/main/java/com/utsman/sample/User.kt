package com.utsman.sample

import com.google.gson.annotations.SerializedName

data class User(
    val success: Boolean,
    @SerializedName("total_page")
    val totalPages: Int,
    val data: List<UserItem>?
) {
    data class UserItem(
        val id: String,
        val name: String
    ) {

        fun toSampleUser(): SampleUser {
            return SampleUser(
                id = id,
                name = name
            )
        }
    }
}