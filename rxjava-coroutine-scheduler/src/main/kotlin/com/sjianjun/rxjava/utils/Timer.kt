package com.sjianjun.rxjava.utils

import com.sjianjun.rxjava.scheduler.CoroutineScheduler
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import java.lang.Exception
import java.util.concurrent.TimeUnit
import kotlin.math.max

class Timer(var scheduler: Scheduler = CoroutineScheduler.Default) {
    fun submit(
        runner: Runnable
    ): Disposable {
        return submit({ runner.run() })
    }

    fun submit(
        runner: Runnable,
        delay: Long
    ): Disposable {
        return submit({ runner.run() }, delay)
    }

    fun submit(
        runner: Runnable,
        delay: Long,
        period: Long
    ): Disposable {
        return submit({ runner.run() }, delay, period)
    }

    fun submit(
        runner: () -> Unit,
        delay: Long = 0,
        period: Long = -1,
        onError: (e: Exception) -> Unit = {}
    ): Disposable {
        if (period <= 0L) {
            return this.scheduler.scheduleDirect({
                try {
                    runner()
                } catch (e: Exception) {
                    onError(e)
                }
            }, max(delay, 0L), TimeUnit.MILLISECONDS)
        } else {
            return this.scheduler.schedulePeriodicallyDirect({
                try {
                    runner()
                } catch (e: Exception) {
                    onError(e)
                }
            }, max(delay, 0L), period, TimeUnit.MILLISECONDS)
        }
    }
}