# AndroidUtils
android 私人工具类集合

## Logger

文件日志类，任何高频读写情况下都能保证文件日志写入的稳定性。



## PermissionHelper 

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