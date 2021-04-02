package com.utsman.sample

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.utsman.paging.data.PagingData

class SampleViewModel : ViewModel() {

    val sampleDataSource = SampleDataSource()
    val pageData: LiveData<PagingData<SampleItem>> = sampleDataSource.currentPageLiveData()
}