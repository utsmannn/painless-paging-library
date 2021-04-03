package com.utsman.sample

import com.utsman.paging.data.PagingResult
import com.utsman.paging.datasource.PagingDataSource

class SampleDataSource : PagingDataSource<SampleUser>() {

    private val listCount = 1..20

    private val userRepository: UserRepository = UserRepository.Companion.Impl()

    override suspend fun onLoadState(page: Int): PagingResult {
        /*val listItem = listCount.map {
            SampleUser(
                id = "$it",
                name = "item of $it"
            )
        }

        when (page) {
            1, 2 -> setCallbackItems(listItem)
            3 -> endPage()
        }

        logi("page --> $page")*/

        return try {
            val response = userRepository.getUsers(page)
            val pageSize = response.totalPages
            logi(response.toString())

            val items = response.data?.map { it.toSampleUser() }
            setCallbackItems(items ?: emptyList())
            PagingResult.Success
        } catch (e: Throwable) {
            PagingResult.Error(e)
        }
    }
}