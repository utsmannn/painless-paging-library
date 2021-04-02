package com.utsman.paging

import android.util.Log
import androidx.recyclerview.widget.DiffUtil

internal fun logi(msg: String) = Log.i("PAGING", msg)

internal fun DiffUtil.Callback?.withNull(action: () -> Unit) {
    if (this == null) {
        action()
    }
}

internal fun DiffUtil.Callback?.withoutNull(action: (DiffUtil.Callback) -> Unit) {
    if (this != null) {
        action(this)
    }
}

fun <T>List<T>.toPagingData(): PagingData<T> {
    return PagingData(
        items = this
    )
}

fun <T>Throwable.toPagingData(): PagingData<T> {
    return PagingData(
        items = emptyList(),
        throwable = this
    )
}