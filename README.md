<p align="center">
  <h1 align="center">Painless Paging Library</h1>
</p>

<p align="center">
  <img src="https://images.unsplash.com/photo-1566227675319-d3498bb2b584?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80"/>
</p>

<p align="center">
  <a href="https://jitpack.io/#utsmannn/painless-paging-library"><img alt="bintray" src="https://jitpack.io/v/utsmannn/painless-paging-library.svg"></a>
  <a href="LICENSE"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"></a>
  <a href="https://github.com/utsmannn/painless-paging-library/pulls"><img alt="Pull request" src="https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat"></a>
  <a href="https://twitter.com/utsmannn"><img alt="Twitter" src="https://img.shields.io/twitter/follow/utsmannn"></a>
  <a href="https://github.com/utsmannn"><img alt="Github" src="https://img.shields.io/github/followers/utsmannn?label=follow&style=social"></a>
  <p align="center">Android Paging Library with painless implementation. <br>Build for modern architecture with Kotlin and Coroutine</p>
</p>

## Download
### Step 1 - Add the JitPack repository to your build file
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2 - Add the dependency
```groovy
dependencies {
     implementation 'com.github.utsmannn:painless-paging-library:1.0.0'
}
```

## Implementation
### Create Adapter
The adapter extend to `PainlessPagedAdapter`
```kotlin

// standard view holder
class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(item: User, position: Int) = itemView.run {
        findViewById<TextView>(R.id.txt_item).text = "$position - ${item.name}"
    }
}

// adapter with PainlessPagedAdapter
class UserAdapter : PainlessPagedAdapter<User, UserViewHolder>() {
    override fun onCreatePageViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false))
    }

    override fun onBindPageViewHolder(holder: UserViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it, position)
        }
    }

}
```

### Create data source
Create class with extend to `PagingDataSource`

```kotlin
class UserDataSource : PagingDataSource<User>() {
    private val userRepository: UserRepository = UserRepository.Companion.Impl()

    override suspend fun onLoadState(page: Int): PagingResult {
        return try {
            val response = userRepository.getUsers(page)
            val items = response.data
            PagingResult.Success(items ?: emptyList())
        } catch (e: Throwable) {
            PagingResult.Error(e)
        }
    }
}
```

### Add in ViewModel
The data source will be extracting result item with `PagingData` class and wrapping with `liveData`, implement it on `ViewModel`.

```kotlin
class UserViewModel : ViewModel() {

    private val userDataSource = UserDataSource()
    val pageData: LiveData<PagingData<User>>
            get() = userDataSource.currentPageLiveData()
}
```

### Submitting item on adapter
```kotlin
viewModel.pageData.observe(this) { pagingData ->
    userAdapter.submitData(pagingData)
}
```

## Extensions
This library can generate adapter with simple extensions code. **Not recommended for multiple view type adapter**.

```kotlin
val userAdapter = recyclerView.createSimpleAdapter<User>(R.layout.item_view) {
    layoutManager = LinearLayoutManager(this@MainActivity)
    onBindViewHolder = { itemView, item, position ->
        itemView.run {
            findViewById<TextView>(R.id.txt_item).text = "$position - ${item.name}"
        }
    }
}
```

## Add State changes UI
You can add the loading view in footer, state of data changes.

### Create a standard view holder
Place all view when data change to loading and error

```kotlin
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
```

### Attach state view holder in adapter
```kotlin
sampleAdapter.attachStateViewHolder { parent  ->
    val view = LayoutInflater.from(parent.context).inflate(R.layout.state_view, parent, false)
    StateViewHolder(view)
}

sampleAdapter.onBindLoadStateViewHolder<StateViewHolder> { holder, loadState ->
    holder.bind(loadState) {
        sampleAdapter.retry()
    }
}
```

## License
```
Copyright 2021 Muhammad Utsman

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
---