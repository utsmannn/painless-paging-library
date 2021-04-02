package com.utsman.paging

data class PagingData<T>(
    @JvmField
    var items: List<T> = emptyList(),
    @JvmField
    var throwable: Throwable? = null
)