package com.utsman.sample

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.utsman.paging.PagingData

class SampleViewModel : ViewModel() {

    val sampleDataSource = SampleDataSource()
    val listItem: LiveData<PagingData<SampleItem>> = sampleDataSource.loadCurrentList()
}