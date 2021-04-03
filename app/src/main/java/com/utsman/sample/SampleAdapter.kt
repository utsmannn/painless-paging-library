package com.utsman.sample

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.utsman.paging.data.LoadState
import com.utsman.paging.data.LoadStatus
import com.utsman.paging.adapter.PainlessPagedAdapter

fun logi(msg: String) = Log.i("LOGGING", msg)

data class SampleUser(
    var id: String = "",
    var name: String = ""
)

class SampleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    @SuppressLint("SetTextI18n")
    fun bind(user: SampleUser, position: Int) = itemView.run {
        findViewById<TextView>(R.id.txt_item).text = "$position - ${user.name}"
    }
}

class StateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(loadState: LoadState, retry: () -> Unit) = itemView.run {
        logi("state in holder is --> ${loadState.loadStatus}")
        logi("bind success.......")
        findViewById<ProgressBar>(R.id.progress_bar).run {
            isVisible = loadState.loadStatus == LoadStatus.RUNNING
        }

        findViewById<TextView>(R.id.txt_error).run {
            isVisible = loadState.loadStatus == LoadStatus.FAILED

            if (loadState.loadStatus == LoadStatus.FAILED) {
                text = (loadState as LoadState.Failed).throwable?.message
            }
        }

        findViewById<Button>(R.id.btn_retry).run {
            isVisible = loadState.loadStatus == LoadStatus.FAILED
            setOnClickListener {
                retry.invoke()
            }
        }
    }
}

class SampleAdapter : PainlessPagedAdapter<SampleUser, SampleViewHolder>() {
    override fun onCreatePageViewHolder(parent: ViewGroup, viewType: Int): SampleViewHolder {
        return SampleViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        )
    }

    override fun onBindPageViewHolder(holder: SampleViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it, position)
        }
    }

}