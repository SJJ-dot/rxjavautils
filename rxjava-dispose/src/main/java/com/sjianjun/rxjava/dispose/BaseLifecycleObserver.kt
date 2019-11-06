package com.sjianjun.rxjava.dispose

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.Disposable
import java.util.concurrent.ConcurrentHashMap

private val map = ConcurrentHashMap<String, BaseLifecycleObserver>()

class BaseLifecycleObserver(
    val disposable: Disposable,
    private val lifecycle: Lifecycle,
    private val key: String? = null,
    private val event: Lifecycle.Event = Lifecycle.Event.ON_DESTROY
) :
    LifecycleObserver {
    init {
        if (key != null) {
            val observer = map.remove(key)
            map[key] = this
            if (observer != null) {
                observer.disposable.dispose()
                lifecycle.removeObserver(observer)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        if (event == Lifecycle.Event.ON_PAUSE) {
            disposable.dispose()
        }
        clean()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        if (event == Lifecycle.Event.ON_STOP) {
            disposable.dispose()
        }
        clean()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        if (event == Lifecycle.Event.ON_DESTROY) {
            disposable.dispose()
        }
        clean()
    }

    private fun clean() {
        if (disposable.isDisposed) {
            lifecycle.removeObserver(this)
            if (key != null)
                map.remove(key)
        }
    }


}