package com.sjianjun.rxjava.utils

import com.sjianjun.rxjava.scheduler.CoroutineScheduler

class Timers {
    companion object {
        @JvmField
        val DEFAULT = Timer(CoroutineScheduler.Default)

        @JvmField
        val IO = Timer(CoroutineScheduler.IO)

        @JvmField
        val Main = Timer(CoroutineScheduler.Main)

        @JvmField
        val Unconfined = Timer(CoroutineScheduler.Unconfined)
    }
}