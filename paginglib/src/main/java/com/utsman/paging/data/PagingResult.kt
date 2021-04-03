package com.utsman.paging.data

sealed class PagingResult(val loadState: LoadState) {
    object Success : PagingResult(LoadState.Success)
    data class Error(val throwable: Throwable?) : PagingResult(LoadState.error(throwable?.message))
}