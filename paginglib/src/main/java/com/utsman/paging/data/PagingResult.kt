package com.utsman.paging.data

sealed class PagingResult(val loadState: LoadState) {
    data class Success<T>(val items: List<T>): PagingResult(LoadState.Success)
    data class Error(val throwable: Throwable?) : PagingResult(LoadState.error(throwable?.message))
}