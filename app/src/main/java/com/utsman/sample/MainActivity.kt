package com.utsman.sample

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.utsman.paging.data.LoadState
import com.utsman.paging.data.LoadStatus
import com.utsman.paging.extensions.createSimpleAdapter

class MainActivity : AppCompatActivity() {

    private val sampleAdapter = SampleAdapter()
    private val viewModel: SampleViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        /*val sampleAdapter = recyclerView.createSimpleAdapter<SampleUser>(R.layout.item_view) {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            onBindViewHolder = { v, item, p ->
                v.run {
                    findViewById<TextView>(R.id.txt_item).text = "$p - ${item.name}"
                }
            }
        }*/

        recyclerView.run {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = sampleAdapter
        }

        sampleAdapter.attachStateViewHolder { parent  ->
            val view = LayoutInflater.from(parent.context).inflate(R.layout.state_view, parent, false)
            StateViewHolder(view)
        }

        sampleAdapter.onBindLoadStateViewHolder<StateViewHolder> { holder, loadState ->
            holder.bind(loadState) {
                sampleAdapter.retry()
            }
        }

        sampleAdapter.addOnLoadStateListener { itemCount, state ->
            val isStarting = itemCount == 0 && state == LoadState.Running
            val isFailed = itemCount == 0 && state.loadStatus == LoadStatus.FAILED
            val reasonError = if (isFailed) {
                (state as LoadState.Failed).throwable?.message
            } else {
                ""
            }

            findViewById<ProgressBar>(R.id.progress_bar).isVisible = isStarting
            findViewById<TextView>(R.id.txt_error).run {
                isVisible = isFailed
                text = reasonError
            }
            findViewById<Button>(R.id.btn_retry).run {
                isVisible = itemCount == 0 && isFailed
                setOnClickListener {
                    sampleAdapter.retry()
                }
            }
            recyclerView.isVisible = !isFailed
            logi("state in activity --> count: $itemCount | origin: ${sampleAdapter.itemList.size} | ${state.loadStatus}")
        }

        viewModel.pageData.observe(this) {
            sampleAdapter.submitData(it)
        }
    }
}