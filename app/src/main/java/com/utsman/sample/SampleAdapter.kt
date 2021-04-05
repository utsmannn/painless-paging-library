package com.utsman.sample

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.utsman.paging.data.LoadState
import com.utsman.paging.data.LoadStatus
import com.utsman.paging.adapter.PainlessPagedAdapter

fun logi(msg: String) = Log.i("LOGGING", msg)

fun Int.isOdd(): Boolean {
    return this % 2 == 0
}

enum class UserType {
    RED, GRAY
}

data class SampleUser(
    var id: String = "",
    var name: String = "",
    var type: UserType = UserType.RED
)

class OddViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    @SuppressLint("SetTextI18n")
    fun bind(user: SampleUser, position: Int) = itemView.run {
        setBackgroundColor(Color.RED)
        findViewById<TextView>(R.id.txt_item).text = "$position - ${user.name}"
    }
}

class EvenViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    @SuppressLint("SetTextI18n")
    fun bind(user: SampleUser, position: Int) = itemView.run {
        setBackgroundColor(Color.GRAY)
        findViewById<TextView>(R.id.txt_item).text = "$position - ${user.name}"
    }
}

class StateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(loadState: LoadState, retry: () -> Unit) = itemView.run {
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

class SampleAdapter : PainlessPagedAdapter<SampleUser, RecyclerView.ViewHolder>() {
    override fun onCreatePageViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 2) {
            OddViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
            )
        } else {
            EvenViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
            )
        }
    }

    override fun onBindPageViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let {
            //holder.bind(it, position)
            when (getItemViewType(position)) {
                2 -> (holder as OddViewHolder).bind(it, position)
                else -> (holder as EvenViewHolder).bind(it, position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {

        val item = getItem(position)
        return if (item != null) {
            when (item.type) {
                UserType.RED -> 1
                UserType.GRAY -> 2
            }
        } else {
            super.getItemViewType(position)
        }
    }

}