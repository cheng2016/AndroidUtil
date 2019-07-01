# AndroidUtils
android 私人工具类集合

## [Logger](util/Logger.java)

文件日志类，任何高频读写情况下都能保证文件日志写入的稳定性。



## [PermissionHelper](util/PermissionHelper.java) 

权限申请统一处理类

```
PermissionHelper mHelper = new PermissionHelper(this);
mHelper.requestPermissions("请授予[录音]，[读写]权限，否则无法录音",
                new PermissionHelper.PermissionListener() {
                    @Override
                    public void doAfterGrand(String... permission) {
  						//成功回调
                    }
                    @Override
                    public void doAfterDenied(String... permission) {
                        //失败回调
                    }
                }, Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE);
```


## [OkHttpUtil](util/OkHttpUtil.java)

一个强大的Okhttp的工具类，参考Async-Http、Retrofit2、okhttp3源码通信库写出的Okhttp工具类。主要解决小项目中的http请求，避免与Rxjava、Retrofit耦合小才大用的问题。经测试性能比Rxjava+retrofit+okhttp内存上开销上少一半左右。


## [CrashHandler](util/CrashHandler.java)

一个app崩溃日志收集类，把App崩溃日志写入SD卡中。

