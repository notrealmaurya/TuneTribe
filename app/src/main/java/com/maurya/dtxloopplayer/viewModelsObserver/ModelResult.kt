package com.maurya.dtxloopplayer.viewModelsObserver

sealed class ModelResult<T>(val data: T? = null, val message: String? = null) {

    class Success<T>(data: T) : ModelResult<T>(data)
    class Error<T>(message: String?, data: T? = null) : ModelResult<T>(data, message)
    class Loading<T> : ModelResult<T>()
}