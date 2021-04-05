package com.utsman.paging.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.utsman.paging.adapter.PainlessPagedAdapter

data class GenerateSimpleAdapter<T>(
    var layoutManager: RecyclerView.LayoutManager,
    var onBindViewHolder: ((itemView: View, item: T, position: Int) -> Unit)? = null
)

class SimpleViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(item: T, position: Int, viewHolder: View.(T, Int) -> Unit) = itemView.run {
        viewHolder.invoke(this, item, position)
    }
}

class SimpleAdapter<T>(private val layoutRes: Int, private val onBind: ((View, T, Int) -> Unit)?) :
    PainlessPagedAdapter<T, SimpleViewHolder<T>>() {

    override fun onCreatePageViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder<T> {
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return SimpleViewHolder(view)
    }

    override fun onBindPageViewHolder(holder: SimpleViewHolder<T>, position: Int) {
        getItem(position)?.let {
            holder.bind(it, position) { _, pos ->
                onBind?.invoke(this, it, pos)
            }
        }
    }
}

fun <T> RecyclerView.createSimpleAdapter(layoutRes: Int, onGenerateSimpleAdapter: GenerateSimpleAdapter<T>.() -> Unit): SimpleAdapter<T> {
    val layoutManagerExist = layoutManager ?: LinearLayoutManager(context)
    val generate = GenerateSimpleAdapter<T>(layoutManagerExist).apply(onGenerateSimpleAdapter)
    val onBind = generate.onBindViewHolder
    return SimpleAdapter(layoutRes, onBind).apply {
        layoutManager = generate.layoutManager
        adapter = this
    }
}