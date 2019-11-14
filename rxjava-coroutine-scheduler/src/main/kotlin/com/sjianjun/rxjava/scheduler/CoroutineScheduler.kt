package com.sjianjun.rxjava.scheduler

import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.EmptyDisposable
import io.reactivex.internal.disposables.SequentialDisposable
import io.reactivex.internal.queue.MpscLinkedQueue
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.coroutines.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

class CoroutineScheduler(private val context: CoroutineContext = Dispatchers.IO) : Scheduler() {


    override fun createWorker(): Worker {
        return CoroutineWorker(context)
    }

    class CoroutineWorker(private val context: CoroutineContext) : Worker() {
        @Volatile
        private var disposed: Boolean = false
        private val coroutine = CoroutineScope(context)
        private val seqCountDownLatch = AtomicReference<CountDownLatch?>()
        private val queue: MpscLinkedQueue<ScheduledRunnable> = MpscLinkedQueue()
        private val tempQueue: MpscLinkedQueue<ScheduledRunnable> = MpscLinkedQueue()
        private var wip = AtomicInteger()
        private val coroutineDelay = AtomicBoolean(false)

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
            val decoratedRun = ScheduledRunnable(RxJavaPlugins.onSchedule(runnable), timeUnit.toMillis(l) + System.currentTimeMillis())

            queue.offer(decoratedRun)
            if (wip.getAndIncrement() == 0 || coroutineDelay.get() && coroutineDelay.compareAndSet(true, false)) {
                coroutine.launch {
                    var missed = 1
                    val q = queue;
                    while (true) {
                        if (disposed) {
                            q.clear()
                            return@launch
                        }

                        while (true) {
                            var delay = 0L
                            while (true) {
                                val run = q.poll() ?: break
                                if (!run.get()) {
                                    val difTime = run.time - System.currentTimeMillis()

                                    if (difTime > 0) {
                                        tempQueue.offer(run)
                                        delay = max(difTime, delay)
                                    } else {
                                        sequentialDisposable.replace(schedule(run))
                                    }
                                    if (disposed) {
                                        q.clear()
                                        return@launch
                                    }
                                }

                            }

                            val count = wip.get()

                            while (true) {
                                val run = tempQueue.poll() ?: break
                                q.offer(run)
                            }

                            if (disposed) {
                                q.clear()
                                return@launch
                            }

                            if (wip.get() != count) {
                                continue
                            }

                            if (delay == 0L) {
                                break
                            } else {
                                try {
                                    coroutineDelay.lazySet(true)
                                    delay(delay)
                                    if (!coroutineDelay.compareAndSet(true, false)) {
                                        return@launch
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            if (disposed) {
                                q.clear()
                                return@launch
                            }
                        }


                        missed = wip.addAndGet(-missed)
                        if (missed == 0) {
                            return@launch
                        }

                    }
                }
            }
            sequentialDisposable.replace(decoratedRun)
            return sequentialDisposable
        }

        override fun dispose() {
            if (!disposed) {
                disposed = true
                coroutine.cancel()
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
