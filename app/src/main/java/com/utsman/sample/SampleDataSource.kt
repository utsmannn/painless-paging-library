package com.utsman.sample

import com.utsman.paging.datasource.PagingDataSource

class SampleDataSource : PagingDataSource<SampleItem>() {

    private val listCount = 1..20

    override suspend fun onLoadState(page: Int) {
        val listItem = listCount.map {
            SampleItem(
                id = "$it",
                name = "item of $it"
            )
        }

        when (page) {
            1, 2 -> setCallbackItems(listItem)
            3 -> setCallbackError(Throwable("Error test"))
        }

        logi("page --> $page")
    }
}