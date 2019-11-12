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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
            CoroutineScope(Dispatchers.IO).launch {
                (0 until 2000).forEach {index->
                    CoroutineScope(Dispatchers.IO).launch {
                        Observable.just("测试Observable扩展绑定生命周期$index :")
                            .delay(5000, TimeUnit.MILLISECONDS, CoroutineScheduler.Default)
                            .subscribe {
                                Log.e("${Thread.currentThread()} ${Thread.currentThread().id}")
                                var i = 0.0
                                var max: Int = (Math.random() * 10000).toInt()
                                (0 until max).forEach {
                                    i += Math.random()
                                }
                                count1.incrementAndGet()
                            }
                    }
                }
            }


            var count = 0
            Timers.defaultScheduler = Schedulers.computation()
            Timers.submit({
                Log.e("${count1.get()}period ${Thread.currentThread()} is UI thread ${Looper.getMainLooper().thread == Thread.currentThread()}")
            }, 2000, 2000).pause("coroutineSchedulerConcurrentTest")
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
