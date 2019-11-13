package com.sjianjun.rxjava.scheduler

import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.EmptyDisposable
import io.reactivex.internal.disposables.SequentialDisposable
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.coroutines.*
import java.util.*
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
        private val coroutine = CoroutineScope(context)
        private val delayCoroutine = CoroutineScope(Dispatchers.Unconfined)

        private val seqCountDownLatch = AtomicReference<CountDownLatch?>()
        override fun schedule(runnable: Runnable): Disposable {
            if (isDisposed) {
                return EmptyDisposable.INSTANCE
            }

            val decoratedRun = ScheduledRunnable(RxJavaPlugins.onSchedule(runnable))
            val current = CountDownLatch(1)
            var preCountDownLatch: CountDownLatch?
            do {
                preCountDownLatch = seqCountDownLatch.get()
            } while (!seqCountDownLatch.compareAndSet(preCountDownLatch, current))

            val job = coroutine.launch {
                try {
                    if (preCountDownLatch?.count != 0L) {
                        preCountDownLatch?.await()
                    }
                    decoratedRun.run()
                } catch (e: CancellationException) {
//                    nothing to do
                    e.printStackTrace()
                } catch (e: Throwable) {
                    RxJavaPlugins.onError(e)
                } finally {
                    current.countDown()
                }
            }
            return CoroutineDispose(job, decoratedRun)
        }

        override fun schedule(runnable: Runnable, l: Long, timeUnit: TimeUnit): Disposable {
            if (l <= 0) {
                return schedule(runnable)
            }
            if (isDisposed) {
                return EmptyDisposable.INSTANCE
            }
            val sequentialDisposable = SequentialDisposable()
            val decoratedRun = ScheduledRunnable(RxJavaPlugins.onSchedule(runnable))
            val job = delayCoroutine.launch {
                try {
                    if (l > 0) {
                        delay(timeUnit.toMillis(l))
                    }
                    sequentialDisposable.replace(schedule(decoratedRun))
                } catch (e: CancellationException) {
//                    nothing to do
                    e.printStackTrace()
                } catch (e: Throwable) {
                    RxJavaPlugins.onError(e)
                }
            }
            sequentialDisposable.replace(CoroutineDispose(job, decoratedRun))
            return sequentialDisposable
        }

        override fun dispose() {
            if (!disposed) {
                disposed = true
                coroutine.cancel()
                delayCoroutine.cancel()
            }
        }

        override fun isDisposed(): Boolean {
            return disposed
        }

    }

    private class CoroutineDispose(
        private val job: Job,
        private val disposable: Disposable
    ) :
        AtomicBoolean(), Disposable {
        override fun isDisposed(): Boolean = get()

        override fun dispose() {
            if (compareAndSet(false, true)) {
                job.cancel()
                disposable.dispose()
            }
        }
    }

    private class ScheduledRunnable(
        val actual: Runnable,
        val time: Long = 0
    ) : AtomicBoolean(), Runnable, Disposable {
        override fun isDisposed(): Boolean = get()

        override fun dispose() {
            lazySet(true)
        }

        override fun run() {
            if (get()) {
                return
            }
            try {
                actual.run()
            } finally {
                lazySet(true)
            }
        }
    }

    private class SeqQueue : LinkedList<ScheduledRunnable>() {
        override fun add(element: ScheduledRunnable): Boolean {
            if (size > 0) {
                synchronized(this) {
                    var index = size - 1
                    while (index >= 0) {
                        if (element.time >= get(index).time) {
                            index++
                            break
                        } else if (index == 0) {
                            break
                        }

                        index--
                    }
                    super.add(index, element)
                    return true
                }
            }
            return super.add(element)
        }
    }

    companion object {
        @JvmStatic
        val IO = CoroutineScheduler(Dispatchers.IO)

        @JvmStatic
        val Main = CoroutineScheduler(Dispatchers.Main)

        @JvmStatic
        val Default = CoroutineScheduler(Dispatchers.Default)

        @JvmStatic
        val Unconfined = CoroutineScheduler(Dispatchers.Unconfined)
    }

}
