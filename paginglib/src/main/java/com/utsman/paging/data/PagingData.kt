package com.utsman.paging.data

data class PagingData<T>(
    @JvmField
    internal var items: List<T> = emptyList(),
    @JvmField
    internal var throwable: Throwable? = null
)