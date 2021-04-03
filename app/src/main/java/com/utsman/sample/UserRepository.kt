package com.utsman.sample

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface UserRepository {
    suspend fun getUsers(page: Int): User

    companion object {

        object Network {
            private const val baseUrl = "https://nama-indo.herokuapp.com/"
            const val get = "/name"

            private val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            private fun provideOkHttp() = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()

            fun retrofit(): Retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(provideOkHttp())
                .build()

        }

        interface Service {
            @GET(Network.get)
            suspend fun getUser(
                @Query("page") page: Int
            ): User
        }

        class Impl : UserRepository {
            private val service by lazy {
                Network.retrofit().create(Service::class.java)
            }

            override suspend fun getUsers(page: Int): User {
                return service.getUser(page)
            }
        }
    }
}