package com.sjianjun.rxjava.dispose

import androidx.lifecycle.Lifecycle
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.Disposable


fun Disposable.destroy(key: String? = null, lifecycle: Lifecycle) {
    lifecycle.addObserver(BaseLifecycleObserver(this, lifecycle, key, Lifecycle.Event.ON_DESTROY))
}

fun Disposable.stop(key: String? = null, lifecycle: Lifecycle) {
    lifecycle.addObserver(BaseLifecycleObserver(this, lifecycle, key, Lifecycle.Event.ON_STOP))
}

fun Disposable.pause(key: String? = null, lifecycle: Lifecycle) {
    lifecycle.addObserver(BaseLifecycleObserver(this, lifecycle, key, Lifecycle.Event.ON_PAUSE))
}

fun <T> destroy(key: String?=null,lifecycle: Lifecycle):ObservableTransformer<T,T> {
    return ObservableTransformer{
        it.doOnSubscribe {
            lifecycle.addObserver(BaseLifecycleObserver(it, lifecycle, key, Lifecycle.Event.ON_DESTROY))
        }
    }
}

fun <T> stop(key: String?=null,lifecycle: Lifecycle):ObservableTransformer<T,T> {
    return ObservableTransformer{
        it.doOnSubscribe {
            lifecycle.addObserver(BaseLifecycleObserver(it, lifecycle, key, Lifecycle.Event.ON_STOP))
        }
    }
}

fun <T> pause(key: String?=null,lifecycle: Lifecycle):ObservableTransformer<T,T> {
    return ObservableTransformer{
        it.doOnSubscribe {
            lifecycle.addObserver(BaseLifecycleObserver(it, lifecycle, key, Lifecycle.Event.ON_PAUSE))
        }
    }
}