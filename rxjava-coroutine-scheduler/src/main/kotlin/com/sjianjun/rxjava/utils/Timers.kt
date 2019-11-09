package com.sjianjun.rxjava.utils

import com.sjianjun.rxjava.scheduler.CoroutineScheduler
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import java.lang.Exception
import java.util.concurrent.TimeUnit
import kotlin.math.max

object Timers {
    var defaultScheduler = CoroutineScheduler.Default
    @JvmStatic
    @JvmOverloads
    fun submit(
        runner: Runnable,
        delay: Long = 0,
        period: Long = -1,
        onError: (e: Exception) -> Unit = {},
        scheduler: Scheduler = defaultScheduler
    ): Disposable {
        return submit({ runner.run() }, delay, period, onError, scheduler)
    }

    @JvmStatic
    fun submit(
        runner: Runnable,
        scheduler: Scheduler
    ): Disposable {
        return submit({ runner.run() }, scheduler = scheduler)
    }

    @JvmStatic
    fun submit(
        runner: Runnable,
        delay: Long,
        scheduler: Scheduler
    ): Disposable {
        return submit({ runner.run() }, delay, scheduler = scheduler)
    }

    @JvmStatic
    fun submit(
        runner: Runnable,
        delay: Long,
        period: Long,
        scheduler: Scheduler
    ): Disposable {
        return submit({ runner.run() }, delay, period, scheduler = scheduler)
    }

    fun submit(
        runner: () -> Unit,
        delay: Long = 0,
        period: Long = -1,
        onError: (e: Exception) -> Unit = {},
        scheduler: Scheduler = defaultScheduler
    ): Disposable {
        if (period <= 0L) {
            return scheduler.scheduleDirect({
                try {
                    runner()
                } catch (e: Exception) {
                    onError(e)
                }
            }, max(delay, 0L), TimeUnit.MILLISECONDS)
        } else {
            return scheduler.schedulePeriodicallyDirect({
                try {
                    runner()
                } catch (e: Exception) {
                    onError(e)
                }
            }, max(delay, 0L), period, TimeUnit.MILLISECONDS)
        }
    }
}