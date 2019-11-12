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
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import sjj.alog.Log
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : AppCompatActivity(), AutoDispose {

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testLifecycleBindDispose()
        coroutineSchedulerConcurrentTest()
    }


    private fun coroutineSchedulerConcurrentTest() {
        concurrent_test.setOnClickListener {
            val count1 = AtomicInteger()

            (0..5).forEach {
                Observable.just("测试Observable扩展绑定生命周期$it :")
                    .delay(10000, TimeUnit.MILLISECONDS, CoroutineScheduler.IO)
                    .subscribe {
                        Log.e(it + Thread.currentThread())
                        count1.incrementAndGet()
                    }
            }

            var count = 0
            Timers.defaultScheduler =Schedulers.computation()
            Timers.submit({
                if (++count < 8) {
                    Log.e("${count1.get()}period ${Thread.currentThread()} is UI thread ${Looper.getMainLooper().thread == Thread.currentThread()}")
                }
            }, 2000, 2000).pause()
        }
    }

    private fun testLifecycleBindDispose() {
        dispose_test.setOnClickListener {
            Observable.just("测试ObservableTransformer扩展绑定生命周期1")
                .delay(2000, TimeUnit.MILLISECONDS, CoroutineScheduler.IO)
                .doOnDispose {
                    Log.e("doOnDispose 测试ObservableTransformer扩展绑定生命周期1")
                }
                .compose(pause("测试ObservableTransformer扩展绑定生命周期1", lifecycle))
                .subscribe {
                    Log.e(it)
                }
            Observable.just("测试ObservableTransformer扩展绑定生命周期1")
                .delay(10000, TimeUnit.MILLISECONDS, CoroutineScheduler.IO)
                .doOnDispose {
                    Log.e("doOnDispose 测试ObservableTransformer扩展绑定生命周期1")
                }
                .compose(pause("测试ObservableTransformer扩展绑定生命周期1"))
                .subscribe {
                    Log.e(it)
                }
//        //测试Disposable扩展绑定生命周期
            Observable.just("测试Disposable扩展绑定生命周期")
                .delay(2000, TimeUnit.MILLISECONDS, CoroutineScheduler.IO)
                .doOnDispose {
                    Log.e("doOnDispose 测试Disposable扩展绑定生命周期")
                }
                .subscribe {
                    Log.e(it)
                }.pause("测试Disposable扩展绑定生命周期")
            Observable.just("测试Disposable扩展绑定生命周期")
                .delay(10000, TimeUnit.MILLISECONDS, CoroutineScheduler.IO)
                .doOnDispose {
                    Log.e("doOnDispose 测试Disposable扩展绑定生命周期")
                }
                .subscribe {
                    Log.e(it)
                }.pause("测试Disposable扩展绑定生命周期")
//
//        //测试Observable扩展绑定生命周期
            Observable.just("测试Observable扩展绑定生命周期")
                .delay(2000, TimeUnit.MILLISECONDS, CoroutineScheduler.IO)
                .doOnDispose {
                    Log.e("doOnDispose 测试Observable扩展绑定生命周期")
                }.pause("测试Observable扩展绑定生命周期")
                .subscribe {
                    Log.e(it)
                }
            Observable.just("测试Observable扩展绑定生命周期2")
                .delay(10000, TimeUnit.MILLISECONDS, CoroutineScheduler.IO)
                .doOnDispose {
                    Log.e("doOnDispose 测试Observable扩展绑定生命周期")
                }.pause("测试Observable扩展绑定生命周期")
                .subscribe {
                    Log.e(it)
                }
        }
    }

}
