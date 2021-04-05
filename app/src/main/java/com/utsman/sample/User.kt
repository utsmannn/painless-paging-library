package com.utsman.sample

import com.google.gson.annotations.SerializedName
import java.util.*

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
            val lengthType = name.length
            val type = (0..1).random()
            return SampleUser(
                id = id,
                name = name,
                type = UserType.values()[type]
            )
        }
    }
}