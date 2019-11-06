package com.sjianjun.rxjava.dispose

import android.arch.lifecycle.Lifecycle
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.Disposable

interface AutoDispose {

    fun getLifecycle(): Lifecycle


    fun Disposable.destroy(key: String? = null) {
        destroy(key, getLifecycle())
    }

    fun Disposable.stop(key: String? = null) {
        stop(key, getLifecycle())
    }

    fun Disposable.pause(key: String? = null) {
        pause(key, getLifecycle())
    }

    fun <T> destroy(key: String? = null): ObservableTransformer<T, T> {
        return destroy(key, getLifecycle())
    }

    fun <T> stop(key: String? = null): ObservableTransformer<T, T> {
        return stop(key, getLifecycle())
    }

    fun <T> pause(key: String? = null): ObservableTransformer<T, T> {
        return pause(key, getLifecycle())
    }
}
