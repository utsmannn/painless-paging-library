package com.utsman.paging.extensions

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.utsman.paging.data.PagingData

internal fun logi(msg: String) = Log.i("PAGING", msg)

internal fun DiffUtil.Callback?.withNull(action: () -> Unit) {
    if (this == null) {
        action()
    }
}

fun <T>List<T>.toPagingData(): PagingData<T> {
    return PagingData(
        items = this
    )
}

fun <T>PagingData<T>.toList(): List<T> {
    return items
}

internal fun <T>Throwable.toPagingData(): PagingData<T> {
    return PagingData(
        items = emptyList(),
        throwable = this
    )
}