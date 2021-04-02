package com.utsman.paging

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

abstract class PainlessPagedAdapter<T, VH : RecyclerView.ViewHolder>(private var diffUtil: DiffUtil.Callback? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val pagingData = PagingData<T>()
    private val mutableItemList: MutableList<T> = pagingData.items.toMutableList()
    private var diffResult: DiffUtil.DiffResult? = null
    val itemList: List<T> = mutableItemList
    private var loadState: LoadState = LoadState.Idle
    private var loadStateFlow: MutableStateFlow<LoadState> = MutableStateFlow(LoadState.Idle)
    private var stateViewGroup: MutableStateFlow<ViewGroup?> = MutableStateFlow(null)
    private var viewGroup: ViewGroup? = null
    private var stateHolder: RecyclerView.ViewHolder? = null
    private val loadStateType = 99
    private var pagingDataSource: PagingDataSource<T>? = null
    private var delayPerPage: Long = 1000

    fun setDelayPerPage(delay: Long) {
        delayPerPage = delay
    }

    fun submitData(newPagingData: PagingData<T>) = GlobalScope.launch {
        submitLoadState(LoadState.Running)
        delay(delayPerPage)
        MainScope().launch {
            if (newPagingData.throwable == null) {
                diffUtil.withNull {
                    diffUtil = ItemDiffUtil(mutableItemList, newPagingData.items)
                }

                diffResult = DiffUtil.calculateDiff(diffUtil!!)
                mutableItemList.addAll(newPagingData.items)
                diffResult!!.dispatchUpdatesTo(this@PainlessPagedAdapter)
                submitLoadState(LoadState.Success)
            } else {
                submitLoadState(LoadState.error(newPagingData.throwable?.message))
            }
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
                loadState.invoke(itemCount, state)
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
        return when (stateHolder) {
            null -> mutableItemList.size
            else -> getItemCountWithState()-1
        }
    }

    private fun getItemCountWithState(): Int {
        return when (stateHolder) {
            null -> mutableItemList.size
            else -> mutableItemList.size + if (hasExtraRow()) 1 else 0
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

    private fun copyLast(): T {
        return mutableItemList.last()
    }

    private fun addLast() {
        if (mutableItemList.isNotEmpty()) {
            try {
                mutableItemList.add(copyLast())
                notifyItemInserted(itemCount + 1)
            } catch (e: IndexOutOfBoundsException) {
            }
        }
    }

    private fun removeLast() {
        if (mutableItemList.isNotEmpty()) {
            try {
                mutableItemList.removeLast()
                notifyItemChanged(mutableItemList.lastIndex)
            } catch (e: IndexOutOfBoundsException) {
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val layoutManager = recyclerView.layoutManager
        submitLoadState(LoadState.Running)
        if (pagingDataSource != null && layoutManager != null) {
            GlobalScope.launch {
                pagingDataSource!!.loadState(1)
            }

            recyclerView.addOnScrollListener(object : EndlessScrollListener(layoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                    when {
                        pagingDataSource?.endPage == true -> {
                            submitLoadState(LoadState.End)
                        }
                        pagingDataSource?.hasError == true -> {
                            submitLoadState(LoadState.Failed(pagingData.throwable))
                        }
                        else -> {
                            logi("load on end ......")
                            submitLoadState(LoadState.Running)
                            MainScope().launch {
                                pagingDataSource!!.loadState(page + 1)
                            }
                        }
                    }
                }
            })
        }
    }

    fun refresh() {
        mutableItemList.clear()
        notifyDataSetChanged()
        if (pagingDataSource != null) {
            GlobalScope.launch {
                pagingDataSource!!.onLoadState(1)
            }
        }
    }

    fun retry() {
        if (pagingDataSource != null) {
            GlobalScope.launch {
                submitLoadState(LoadState.Running)
                pagingDataSource!!.onLoadState(pagingDataSource!!.currentPage)
            }
        }
    }

    fun bindDataSource(dataSource: PagingDataSource<T>) {
        pagingDataSource = dataSource
    }
}