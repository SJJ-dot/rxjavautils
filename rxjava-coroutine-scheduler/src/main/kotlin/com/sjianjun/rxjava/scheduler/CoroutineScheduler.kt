package com.sjianjun.rxjava.scheduler

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.EmptyDisposable
import io.reactivex.internal.disposables.SequentialDisposable
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.coroutines.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext

class CoroutineScheduler(private val context: CoroutineContext = Dispatchers.IO) : Scheduler() {

    override fun createWorker(): Worker {
        return CoroutineWorker(context)
    }

    class CoroutineWorker(private val context: CoroutineContext) : Worker() {
        @Volatile
        private var disposed: Boolean = false
        private var container = CompositeDisposable()
        private val coroutine = CoroutineScope(context)

        private val seqCountDownLatch = AtomicReference<CountDownLatch?>()
        override fun schedule(runnable: Runnable, l: Long, timeUnit: TimeUnit): Disposable {
            if (isDisposed) {
                return EmptyDisposable.INSTANCE
            }

            val decoratedRun = RxJavaPlugins.onSchedule(runnable)

            val compositeDisposable = CompositeDisposable()
            val sequentialDisposable = SequentialDisposable(compositeDisposable)

            val runner = ScheduledRunnable(decoratedRun, container)
            container.add(runner)
            compositeDisposable.add(runner)

            val current = CountDownLatch(1)
            var preCountDownLatch: CountDownLatch?
            do {
                preCountDownLatch = seqCountDownLatch.get()
            } while (!seqCountDownLatch.compareAndSet(preCountDownLatch, current))

            val job = coroutine.launch {
                try {
                    if (l > 0) {
                        delay(timeUnit.toMillis(l))
                    }
                    if (preCountDownLatch?.count != 0L) {
                        preCountDownLatch?.await()
                    }
                    runner.run()
                    current.countDown()

                } catch (e: CancellationException) {
//                    nothing to do
                    e.printStackTrace()
                } catch (e: Throwable) {
                    RxJavaPlugins.onError(e)
                }
            }
            compositeDisposable.add(CoroutineDispose(job))

            return sequentialDisposable
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

    private class CoroutineDispose(private val job: Job) : Disposable {
        override fun isDisposed(): Boolean = job.isCompleted || job.isCancelled

        override fun dispose() {
            if (!isDisposed) {
                job.cancel()
            }
        }
    }

    private class ScheduledRunnable(
        val actual: Runnable,
        val tasks: CompositeDisposable
    ) : AtomicBoolean(), Runnable, Disposable {
        override fun isDisposed(): Boolean = get()

        override fun dispose() {
            lazySet(true)
            tasks.delete(this)
        }

        override fun run() {
            if (get()) {
                return
            }
            try {
                actual.run()
            } finally {
                lazySet(true)
                tasks.delete(this)
            }
        }
    }

    companion object {
        @JvmStatic
        val IO by lazy { CoroutineScheduler(Dispatchers.IO) }

        @JvmStatic
        val Main by lazy { CoroutineScheduler(Dispatchers.Main) }

        @JvmStatic
        val Default by lazy { CoroutineScheduler(Dispatchers.Default) }
    }

}
