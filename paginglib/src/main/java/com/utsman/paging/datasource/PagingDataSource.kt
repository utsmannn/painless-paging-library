package com.utsman.paging.datasource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.utsman.paging.data.PagingData
import com.utsman.paging.extensions.toPagingData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class PagingDataSource<T> {
    private val mutableList: MutableList<T> = mutableListOf()
    private val mutableLiveData: MutableLiveData<PagingData<T>> = MutableLiveData()
    internal var endPage = false
    internal var hasError = false
    internal var currentPage = 1
    internal var currentThrowable: Throwable? = null

    init {
        GlobalScope.launch {
            loadState(1)
        }
    }

    internal suspend fun loadState(page: Int) {
        currentPage = page
        onLoadState(page)
    }

    abstract suspend fun onLoadState(page: Int)

    fun setCallbackItems(items: List<T>) = GlobalScope.launch {
        endPage = false
        hasError = false
        currentThrowable = null
        mutableList.addAll(items)
        mutableLiveData.postValue(mutableList.toPagingData(this@PagingDataSource))
    }

    fun setCallbackError(throwable: Throwable) = GlobalScope.launch {
        hasError = true
        currentThrowable = throwable
        mutableLiveData.postValue(throwable.toPagingData(this@PagingDataSource))
    }

    fun endPage() {
        endPage = true
    }

    fun currentPageLiveData(): LiveData<PagingData<T>> {
        return mutableLiveData
    }

    fun currentPage(): PagingData<T>? {
        return mutableLiveData.value
    }
}
