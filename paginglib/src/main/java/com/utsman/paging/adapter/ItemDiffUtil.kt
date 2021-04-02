package com.utsman.paging.adapter
import androidx.recyclerview.widget.DiffUtil

internal class ItemDiffUtil<T>(private val oldList: List<T>, private val newList: List<T>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = oldItemPosition == newItemPosition

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = oldItemPosition == newItemPosition

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return Change(oldItem, newItem)
    }

    data class Change<out T>(
        val oldData: T,
        val newData: T
    )
}