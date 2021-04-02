package com.utsman.paging.data

enum class LoadStatus {
    IDLE,
    RUNNING,
    SUCCESS,
    FAILED,
    END
}

sealed class LoadState(val loadStatus: LoadStatus) {
    object Idle : LoadState(LoadStatus.IDLE)
    object Running : LoadState(LoadStatus.RUNNING)
    object Success : LoadState(LoadStatus.SUCCESS)
    object End : LoadState(LoadStatus.END)
    data class Failed(val throwable: Throwable?) : LoadState(LoadStatus.FAILED)

    companion object {
        fun error(msg: String?) = Failed(Throwable(msg))
    }
}