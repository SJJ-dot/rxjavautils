package com.sjianjun.rxjava.scheduler

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.EmptyDisposable
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class CoroutineScheduler(private val context: CoroutineContext = Dispatchers.IO) : Scheduler() {

    override fun createWorker(): Worker {
        return CoroutineWorker(context)
    }

    class CoroutineWorker(private val context: CoroutineContext) : Worker() {
        @Volatile
        var disposed: Boolean = false
        var container = CompositeDisposable()

        override fun schedule(runnable: Runnable, l: Long, timeUnit: TimeUnit): Disposable {

            val job = CoroutineScope(context).launch {

                try {
                    if (l > 0) {
                        delay(timeUnit.toMillis(l))
                    }
                    runnable.run()
                } catch (e: CancellationException) {
//                    nothing to do
                } catch (e: Throwable) {
                    RxJavaPlugins.onError(e)
                }
            }

            val dispose = CoroutineDispose(job)
            container.add(dispose)
            job.invokeOnCompletion {
                container.remove(dispose)
            }

            return if (disposed) {
                EmptyDisposable.INSTANCE
            } else dispose
        }

        override fun dispose() {
            if (!disposed) {
                disposed = true
                container.dispose()
            }
        }

        override fun isDisposed(): Boolean {
            return disposed
        }
    }


    class CoroutineDispose(private val job: Job) : Disposable {
        override fun isDisposed(): Boolean = job.isCompleted || job.isCancelled

        override fun dispose() {
            if (!isDisposed) {
                job.cancel()
            }
        }
    }

    companion object {
        @JvmStatic
        val IO
            get() = CoroutineScheduler(Dispatchers.IO)

        @JvmStatic
        val Main
            get() = CoroutineScheduler(Dispatchers.Main)

        @JvmStatic
        val Default
            get() = CoroutineScheduler(Dispatchers.Default)
    }

}
