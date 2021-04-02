package com.utsman.paging.datasource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.utsman.paging.data.PagingData
import com.utsman.paging.extensions.toPagingData
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
    internal var currentThrowable: Throwable? = null

    override suspend fun loadState(page: Int) {
        currentPage = page
        onLoadState(page)
    }

    abstract suspend fun onLoadState(page: Int)

    fun setCallbackItems(items: List<T>) = GlobalScope.launch {
        hasError = false
        currentThrowable = null
        mutableList.clear()
        mutableList.addAll(items)
        mutableLiveData.postValue(mutableList.toPagingData())
    }

    fun setCallbackError(throwable: Throwable) = GlobalScope.launch {
        hasError = true
        currentThrowable = throwable
        mutableLiveData.postValue(throwable.toPagingData())
    }

    fun endPage() {
        endPage = true
    }

    fun loadCurrentList(): LiveData<PagingData<T>> {
        return mutableLiveData
    }
}
