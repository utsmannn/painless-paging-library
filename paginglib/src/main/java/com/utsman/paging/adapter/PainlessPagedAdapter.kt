package com.utsman.paging.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.utsman.paging.data.LoadState
import com.utsman.paging.data.LoadStatus
import com.utsman.paging.data.PagingData
import com.utsman.paging.datasource.PagingDataSource
import com.utsman.paging.extensions.logi
import com.utsman.paging.listener.EndlessScrollListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

abstract class PainlessPagedAdapter<T, VH : RecyclerView.ViewHolder>(
    private var diffUtil: DiffUtil.Callback? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val pagingData = PagingData<T>()
    private val mutableItemList: MutableList<T> = pagingData.items.toMutableList()
    private var diffResult: DiffUtil.DiffResult? = null
    private var loadState: LoadState = LoadState.Idle
    private var loadStateFlow: MutableStateFlow<LoadState> = MutableStateFlow(LoadState.Idle)
    private var stateViewGroup: MutableStateFlow<ViewGroup?> = MutableStateFlow(null)
    private var viewGroup: ViewGroup? = null
    private var stateHolder: RecyclerView.ViewHolder? = null
    private val loadStateType = 99
    private val dataSourceState: MutableStateFlow<PagingDataSource<T>?> = MutableStateFlow(null)
    private var delayPerPage: Long = 1000

    val itemList: List<T> = mutableItemList
    val dataSize: Int
        get() {
            return itemList.size
        }

    fun setDelayPerPage(delay: Long) {
        delayPerPage = delay
    }

    fun submitData(newPagingData: PagingData<T>) = GlobalScope.launch {
        logi("submitting.... -> $newPagingData")
        submitLoadState(LoadState.Running)
        if (dataSourceState.value == null) {
            dataSourceState.value = newPagingData.dataSource
        }
        delay(delayPerPage)
        MainScope().launch {
            if (newPagingData.throwable == null) {
                if (diffUtil == null) {
                    diffUtil = ItemDiffUtil(mutableItemList, newPagingData.items)
                }

                diffResult = DiffUtil.calculateDiff(diffUtil!!)
                mutableItemList.clear()
                mutableItemList.addAll(newPagingData.items)
                submitLoadState(LoadState.Success)
                diffResult!!.dispatchUpdatesTo(this@PainlessPagedAdapter)
            } else {
                submitLoadState(LoadState.error(newPagingData.throwable?.message))
            }
        }
    }

    private fun calculateDiff(operation: () -> Unit) {
        if (diffUtil != null) {
            diffResult = DiffUtil.calculateDiff(diffUtil!!)
            operation.invoke()
            diffResult!!.dispatchUpdatesTo(this@PainlessPagedAdapter)
        }
    }

    fun getItem(position: Int): T? {
        return if (getItemViewType(position) == loadStateType) {
            null
        } else {
            itemList[position]
        }
    }

    abstract fun onCreatePageViewHolder(parent: ViewGroup, viewType: Int): VH
    abstract fun onBindPageViewHolder(holder: VH, position: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (stateViewGroup.value == null) {
            stateViewGroup.value = parent
        }

        return when (viewType) {
            loadStateType -> if (stateHolder != null) {
                stateHolder!!
            } else {
                onCreatePageViewHolder(parent, viewType)
            }
            else -> onCreatePageViewHolder(parent, viewType)
        }
    }

    private var load: ((RecyclerView.ViewHolder, LoadState) -> Unit)? = null

    private fun onLoadState(holder: RecyclerView.ViewHolder, loadState: LoadState) {
        load?.run {
            invoke(holder, loadState)
            onBindLoadStateViewHolder(this)
        }
    }

    fun attachStateViewHolder(stateViewHolder: (parent: ViewGroup) -> RecyclerView.ViewHolder) {
        MainScope().launch {
            stateViewGroup.collect {
                if (it != null) {
                    viewGroup = it
                    if (viewGroup != null) {
                        stateHolder = stateViewHolder(viewGroup!!)
                    }
                }
            }
        }
    }

    fun <VS : RecyclerView.ViewHolder> onBindLoadStateViewHolder(load: (holder: VS, loadState: LoadState) -> Unit) {
        this.load = load as ((RecyclerView.ViewHolder, LoadState) -> Unit)?
    }

    fun addOnLoadStateListener(loadState: (itemCount: Int, loadState: LoadState) -> Unit) =
        MainScope().launch {
            loadStateFlow.collect { state ->
                loadState.invoke(dataSize, state)
            }
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) != loadStateType) {
            onBindPageViewHolder((holder as VH), position)
        } else {
            if (stateHolder != null) {
                onLoadState(holder, loadState)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (hasExtraRow()) {
            mutableItemList.size + if (hasExtraRow()) 1 else 0
        } else {
            mutableItemList.size
        }
    }

    private fun hasExtraRow() =
        stateHolder != null && loadState.loadStatus != LoadStatus.SUCCESS

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1 && stateHolder != null) {
            loadStateType
        } else {
            super.getItemViewType(position)
        }
    }

    private fun submitLoadState(newLoadState: LoadState) {
        loadStateFlow.value = newLoadState
        val previousState = this.loadState
        val hadExtraRow = hasExtraRow()
        this.loadState = newLoadState
        val hasExtraRow = hasExtraRow()
        MainScope().launch {
            if (hadExtraRow != hasExtraRow) {
                if (hadExtraRow) {
                    notifyItemRemoved(itemCount)
                } else {
                    notifyItemInserted(itemCount)
                }
            } else if (hasExtraRow && previousState != loadState) {
                notifyItemChanged(itemCount - 1)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val layoutManager = recyclerView.layoutManager
        submitLoadState(LoadState.Running)
        GlobalScope.launch {
            dataSourceState.collect { pagingDataSource ->
                if (pagingDataSource != null && layoutManager != null) {
                    recyclerView.addOnScrollListener(object : EndlessScrollListener(layoutManager) {
                        override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                            logi("on page --> ${pagingDataSource.currentPage} -> end -> ${pagingDataSource.endPage}")

                            GlobalScope.launch {
                                delay(20)
                                when {
                                    pagingDataSource.endPage -> {
                                        submitLoadState(LoadState.End)
                                    }
                                    pagingDataSource.hasError -> {
                                        submitLoadState(LoadState.Failed(pagingDataSource.currentThrowable))
                                    }
                                    else -> {
                                        submitLoadState(LoadState.Running)
                                        MainScope().launch {
                                            pagingDataSource.loadState(page + 1)
                                        }
                                    }
                                }
                            }
                        }
                    })
                }
            }
        }
    }

    fun refresh() {
        calculateDiff {
            mutableItemList.clear()
        }
        if (dataSourceState.value != null) {
            GlobalScope.launch {
                dataSourceState.value!!.onLoadState(1)
            }
        }
    }

    fun retry() {
        if (dataSourceState.value != null) {
            GlobalScope.launch {
                submitLoadState(LoadState.Running)
                dataSourceState.value!!.onLoadState(dataSourceState.value!!.currentPage)
            }
        }
    }
}