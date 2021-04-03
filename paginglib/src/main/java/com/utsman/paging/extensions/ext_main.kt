package com.utsman.paging.extensions

import android.annotation.SuppressLint
import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.utsman.paging.data.PagingData

internal fun logi(msg: String) = Log.i("PAGING", msg)

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

internal fun <T>createDefaultItemCallback(): DiffUtil.ItemCallback<T> {
    return object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem == newItem
        }
    }
}