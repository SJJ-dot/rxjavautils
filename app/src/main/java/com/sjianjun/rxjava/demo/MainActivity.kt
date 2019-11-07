package com.sjianjun.rxjava.demo

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sjianjun.rxjava.dispose.AutoDispose
import com.sjianjun.rxjava.dispose.pause
import io.reactivex.Observable
import sjj.alog.Log
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(),AutoDispose {

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.e("test")
        Observable.just("test key")
            .delay(2000, TimeUnit.MILLISECONDS)
            .doOnDispose {
                Log.e("doOnDispose test key")
            }
            .compose(pause("test key", lifecycle))
            .subscribe {
                Log.e(it)
            }
        Observable.just("test key2")
            .delay(10000, TimeUnit.MILLISECONDS)
            .doOnDispose {
                Log.e("doOnDispose test key2")
            }
            .compose(pause("test key"))
            .subscribe {
                Log.e(it)
            }
    }
}
