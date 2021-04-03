package com.utsman.paging.extensions

import android.util.Log
import com.utsman.paging.data.PagingData
import com.utsman.paging.datasource.PagingDataSource

internal fun logi(msg: String) = Log.i("PAGING", msg)

internal fun <T>List<T>.toPagingData(dataSource: PagingDataSource<T>): PagingData<T> {
    return PagingData(
        items = this,
        dataSource = dataSource
    )
}

fun <T>PagingData<T>.toList(): List<T> {
    return items
}

internal fun <T>Throwable.toPagingData(dataSource: PagingDataSource<T>): PagingData<T> {
    return PagingData(
        items = emptyList(),
        throwable = this,
        dataSource = dataSource
    )
}