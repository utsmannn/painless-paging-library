package com.utsman.sample

import com.utsman.paging.data.PagingResult
import com.utsman.paging.datasource.PagingDataSource

class SampleDataSource : PagingDataSource<SampleUser>() {
    private val userRepository: UserRepository = UserRepository.Companion.Impl()

    override suspend fun onLoadState(page: Int): PagingResult {
        return try {
            val response = userRepository.getUsers(page)
            logi(response.toString())

            val items = response.data?.map { it.toSampleUser() }
            PagingResult.Success(items ?: emptyList())
        } catch (e: Throwable) {
            PagingResult.Error(e)
        }
    }
}