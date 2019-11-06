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
### 添加依赖
```groovy
dependencies {
    ...
    implementation 'com.sjianjun:rxjava-dispose:1.1.0'
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