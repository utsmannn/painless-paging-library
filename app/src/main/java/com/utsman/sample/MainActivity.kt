package com.utsman.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.utsman.paging.data.LoadState

class MainActivity : AppCompatActivity() {

    private val sampleAdapter = SampleAdapter()
    private val viewModel: SampleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        findViewById<RecyclerView>(R.id.recycler_view).run {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = sampleAdapter.apply {
                setDelayPerPage(1000)
            }
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
            findViewById<ProgressBar>(R.id.progress_bar).isVisible = isStarting
            logi("state in activity --> count: $itemCount | origin: ${sampleAdapter.itemList.size} | ${state.loadStatus}")
        }

        viewModel.pageData.observe(this) {
            sampleAdapter.submitData(it)
        }
    }
}