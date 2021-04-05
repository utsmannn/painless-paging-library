package com.utsman.paging.datasource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.utsman.paging.data.LoadState
import com.utsman.paging.data.LoadStatus
import com.utsman.paging.data.PagingData
import com.utsman.paging.data.PagingResult
import com.utsman.paging.extensions.logi
import com.utsman.paging.extensions.toPagingData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

abstract class PagingDataSource<T> {
    private val mutableList: MutableList<T> = mutableListOf()
    private val mutableLiveData: MutableLiveData<PagingData<T>> = MutableLiveData()
    internal var endPage = false
    internal var hasError = false
    internal var currentPage = 1
    internal var currentThrowable: Throwable? = null

    init {
        MainScope().launch {
            loadState(1)
        }
    }

    internal suspend fun loadState(page: Int) {
        currentPage = page
        when (val state = onLoadState(page)) {
            is PagingResult.Success<*> -> {
                val items = state.items as List<T>?
                setCallbackItems(items)
            }
            is PagingResult.Error -> {
                currentThrowable = state.throwable
                hasError = true
                mutableLiveData.postValue(state.throwable?.toPagingData(this@PagingDataSource))
            }
        }
    }

    abstract suspend fun onLoadState(page: Int): PagingResult

    private fun setCallbackItems(items: List<T>?) = GlobalScope.launch {
        if (items.isNullOrEmpty()) {
            endPage = true
        } else {
            endPage = false
            hasError = false
            currentThrowable = null
            mutableList.addAll(items)
            mutableLiveData.postValue(mutableList.toPagingData(this@PagingDataSource))
        }
    }

    fun currentPageLiveData(): LiveData<PagingData<T>> {
        return mutableLiveData
    }

    fun currentPage(): PagingData<T>? {
        return mutableLiveData.value
    }
}
