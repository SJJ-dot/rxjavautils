### 添加存储库
- 项目根目录build.gradle添加存储库
```groovy
allprojects {
    repositories {
        ...
        maven {
            url "https://raw.githubusercontent.com/lTBeL/repo/master/repository"
        }
    }
}
```
## rxjava 取消订阅封装。
- 可自动在生命周期destroy stop pause 时取消
- 定义key 在自动取消之前存在的key。**注意：**key必须在整个进程中唯一
### 添加依赖
```groovy
dependencies {
    ...
    implementation 'com.sjianjun:rxjava-dispose:1.1.0'
    //or androidx
    implementation 'com.sjianjun:rxjava-dispose:1.2.0'
}
```

### 示例代码
```kotlin
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
```

## rxjava 协程调度器扩展。拥有更好的并发性能。

### 添加依赖
```groovy
dependencies {
    ...
    implementation 'com.sjianjun:rxjava-coroutine-scheduler:0.0.5'
}
```

### 示例代码。用法与普通的调度器相同
```kotlin
Observable.just("test key2")
    .delay(10000,TimeUnit.MILLISECONDS, CoroutineScheduler.IO)

```