package com.utsman.paging.extensions

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.utsman.paging.data.PagingData
import com.utsman.paging.datasource.PagingDataSource
import com.utsman.paging.listener.EndlessScrollListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal fun logi(msg: String) = Log.i("PAGING", msg)

internal fun <T> List<T>.toPagingData(dataSource: PagingDataSource<T>): PagingData<T> {
    return PagingData(
        items = this,
        dataSource = dataSource
    )
}

fun <T> PagingData<T>.toList(): List<T> {
    return items
}

internal fun <T> Throwable.toPagingData(dataSource: PagingDataSource<T>): PagingData<T> {
    return PagingData(
        items = emptyList(),
        throwable = this,
        dataSource = dataSource
    )
}

internal suspend fun RecyclerView.whenScrollEnd(
    layoutManager: RecyclerView.LayoutManager
): Flow<Int> = callbackFlow {
    val listener = object : EndlessScrollListener(layoutManager) {
        override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
            offer(page + 1)
        }
    }

    addOnScrollListener(listener)
    awaitClose { removeOnScrollListener(listener) }
}
