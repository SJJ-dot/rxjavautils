package com.sjianjun.rxjava.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.sjianjun.rxjava.dispose.AutoDispose
import com.sjianjun.rxjava.dispose.pause
import com.sjianjun.rxjava.scheduler.CoroutineScheduler
import com.sjianjun.rxjava.utils.Timers
import io.reactivex.Observable
import sjj.alog.Log
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), AutoDispose {

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.e("test")
        Observable.just("test key")
            .delay(2000, TimeUnit.MILLISECONDS, CoroutineScheduler.IO)
            .doOnDispose {
                Log.e("doOnDispose test key")
            }
            .compose(pause("test key", lifecycle))
            .subscribe {
                Log.e(it)
            }
        Observable.just("test key2")
            .delay(10000, TimeUnit.MILLISECONDS, CoroutineScheduler.IO)
            .doOnDispose {
                Log.e("doOnDispose test key2")
            }
            .compose(pause("test key"))
            .subscribe {
                Log.e(it)
            }

        Timers.submit({
            Log.e("delay 300 ms ; ${Thread.currentThread()} is UI thread ${Looper.getMainLooper().thread == Thread.currentThread()}")
            Timers.defaultScheduler = CoroutineScheduler.IO
            Timers.submit({
                Log.e("change default scheduler ${Thread.currentThread()} is UI thread ${Looper.getMainLooper().thread == Thread.currentThread()}")
            })
        }, 300, scheduler = CoroutineScheduler.Main)

        Timers.submit({
            Log.e("period ${Thread.currentThread()} is UI thread ${Looper.getMainLooper().thread == Thread.currentThread()}")
        }, 2000, 2000).pause()

    }
}
