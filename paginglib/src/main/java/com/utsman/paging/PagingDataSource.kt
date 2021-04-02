package com.utsman.paging

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal interface PagingSource {
    suspend fun loadState(page: Int)
}

abstract class PagingDataSource<T>: PagingSource {
    private val mutableList: MutableList<T> = mutableListOf()
    private val mutableLiveData: MutableLiveData<PagingData<T>> = MutableLiveData()
    internal var endPage = false
    internal var hasError = false
    internal var currentPage = 1

    override suspend fun loadState(page: Int) {
        currentPage = page
        onLoadState(page)
    }

    abstract suspend fun onLoadState(page: Int)

    fun setCallbackItems(items: List<T>) = GlobalScope.launch {
        mutableList.clear()
        mutableList.addAll(items)
        mutableLiveData.postValue(mutableList.toPagingData())
        hasError = false
    }

    fun setCallbackError(throwable: Throwable) = GlobalScope.launch {
        mutableLiveData.postValue(throwable.toPagingData())
        hasError = true
    }

    fun endPage() {
        endPage = true
    }

    fun loadCurrentList(): LiveData<PagingData<T>> {
        return mutableLiveData
    }
}
