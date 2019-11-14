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
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
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
        coroutineSchedulerConcurrentTest2()

        timers.setOnClickListener {
            Timers.DEFAULT.submit({
                Log.e("test1 ${Thread.currentThread()}")
                Timers.DEFAULT.submit({
                    Log.e("test2 ${Thread.currentThread()}")
                    Timers.DEFAULT.scheduler = AndroidSchedulers.mainThread()
                    Timers.DEFAULT.submit({
                        Log.e("test3 ${Thread.currentThread()} is UI thread: ${Looper.getMainLooper().thread == Thread.currentThread()}")
                    }, 200L, 200L).pause("timers 1")
                }, 200L)
            })

            Timers.Main.submit({
                Log.e("testMain ${Thread.currentThread()} is UI thread: ${Looper.getMainLooper().thread == Thread.currentThread()}")
            }).pause("timers")
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
    }

    private fun coroutineSchedulerConcurrentTest() {
        concurrent_test.setOnClickListener {
            val count1 = AtomicInteger()
            CoroutineScope(Dispatchers.IO).launch {
                (0 until 20000).forEach { index ->
                    CoroutineScope(Dispatchers.IO).launch {
                        Observable.just("测试Observable扩展绑定生命周期$index :")
                            .delay(5000, TimeUnit.MILLISECONDS, CoroutineScheduler.IO)
                            .subscribe {
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


            Timers.Main.submit({
                Log.e("${count1.get()}period ${Thread.currentThread()} is UI thread ${Looper.getMainLooper().thread == Thread.currentThread()}")
            }, 0, 2000).pause("coroutineSchedulerConcurrentTest")
        }
    }

    private var timerJob: Job? = null

    private fun coroutineSchedulerConcurrentTest2() {
        concurrent_test2.setOnClickListener {
            val count = AtomicInteger()
            CoroutineScope(Dispatchers.IO).launch {
                (0 until 20000).forEach {
                    CoroutineScope(Dispatchers.IO).launch {
                        CoroutineScope(Dispatchers.Unconfined).launch {
                            delay(2000)
                            var i = 0.0
                            var max: Int = (Math.random() * 10000).toInt()
                            (0 until max).forEach {
                                i += Math.random()
                            }
                            count.getAndAdd(1)

                        }
                    }
                }
            }
            rrr(count)
        }
    }

    fun rrr(count: AtomicInteger) {
        timerJob = CoroutineScope(Dispatchers.Unconfined).launch {
            delay(2000)
            withContext(Dispatchers.Main) {
                Log.e("${count.get()} period ${Thread.currentThread()} is UI thread ${Looper.getMainLooper().thread == Thread.currentThread()}")
                rrr(count)
            }
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
