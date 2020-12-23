# AndroidUtils
android 私人工具类集合

## [LazyFragment](util/BaseFragment.java)

懒加载fragment

## [Logger](util/Logger.java)

文件日志类，任何高频读写情况下都能保证文件日志写入的稳定性，一次初始化终身使用

    //必须先在Application中初始化文件日志类
    Logger.init(context);
    
    Logger.i(TAG,"this is a logger message");
 
    Logger.e(TAG,"this is a error message",error);

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
权限回调方法必须处理，否则会导致不断拉取权限的问题
```
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (mHelper != null) {
        mHelper.handleRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
```

## [OkHttpUtil](util/OkHttpUtil.java)

一个强大的Okhttp的工具类，参考Async-Http、Retrofit2、okhttp3源码通信库写出的Okhttp工具类。主要解决小项目中的http请求，避免与Rxjava、Retrofit耦合小才大用的问题。经测试性能比Rxjava+retrofit+okhttp内存上开销上少一半左右。

    OkHttpUtil.post(ctx, HttpConfig.SENDMSGTOPHONE_URL, json.toString(), new OkHttpUtil.SimpleResponseHandler() {
                @Override
                public void onSuccess(Call call, Response response) {
                    Log.e("sendMsgToPhone onSuccess","current Thread: " + Thread.currentThread().getName());

                }

                @Override
                public void onFailer(Exception e) {
                    Log.e("sendMsgToPhone onFailer","current Thread: " + Thread.currentThread().getName());
                }
            });
            
## [HttpUtil](util/HttpUtil.java)

一个简易 AsyncHttp 库的工具类。代码有一定参考性。（该库目前以落后，非主流，无维护）

    HttpUtil.post(ctx, HttpConfig.SENDMSGTOPHONE_URL, json.toString(), new OkHttpUtil.SimpleResponseHandler() {
                @Override
                public void onSuccess(Call call, Response response) {
                    Log.e("sendMsgToPhone onSuccess","current Thread: " + Thread.currentThread().getName());

                }

                @Override
                public void onFailer(Exception e) {
                    Log.e("sendMsgToPhone onFailer","current Thread: " + Thread.currentThread().getName());
                }
            });


## [CrashHandler](util/CrashHandler.java)

一个app崩溃日志收集类，把App崩溃日志写入SD卡中。

Use: in application onCreate方法中执行以下方法
    
    CrashHandler.getInstance.init(this);
    
    
## [PreferenceUtils](util/PreferenceUtils.java)

一个简洁易用的SharedPreference工具类。

    PreferenceUtils.setPrefString(MainActivity.this,"key","values");

## [AlipayUtil](util/AlipayUtil.java)

支付宝支付工具类

User:
    
    AlipayUtil.instance().pay(context,json,payListener);



## [Lunar](util/Lunar.java)

公历转农历工具类

    Lunar lunar = new Lunar(calendar);    

    String lunarStr = "";    
    lunarStr=lunar.animalsYear()+"年(";    
    lunarStr +=lunar.cyclical()+"年)";    
    lunarStr +=lunar.toString();    
    tvMsg3.setText(lunarStr);    

    Lunar lunar = new Lunar(calendar); String lunarStr = ""; lunarStr=lunar.animalsYear()+"年("; lunarStr +=lunar.cyclical()+"年)"; lunarStr +=lunar.toString(); tvMsg3.setText(lunarStr);  


## [XmlUtils](util/XmlUtils.java)

xml文件读取、写入工具类


    public static String readLocalXmlConfig(String path) {
            String token = "";
            final File file = new File(path);
            if (file.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(fis, "UTF-8");
                    int eventType = parser.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_DOCUMENT:
                                break;
                            case XmlPullParser.START_TAG:
                                String tagName = parser.getName();
                                if (tagName.equals("token")) {
                                    token = parser.nextText();
                                    Logger.i(TAG, "readLocalXmlConfig 读取成功, token : " + token);
                                }
                                break;
                        }
                        eventType = parser.next();
                    }
                    fis.close();
                } catch (Exception e) {
                    Logger.e(TAG, "readLocalXmlConfig 读取失败 ", e);
                }
            }
            return token;
        }

## [RegexUtils](util/RegexUtils.java)

一个正则工具类

    isMobileExact
    isTelephone
    isIDCard15
    isEmail
    isURL
    isZh
    isUsername


## [DeviceUtils](util/DeviceUtils.java)

    getMacAddress
    getManufacturer
    getModel
    isSDCardEnable
    getSDCardPath
    isPhone
    getDeviceIMEI


## [AppUtils](util/AppUtils.java)

    getVersionName
    getVersionCode
    
## [ResourceUtils](util/ResourceUtils.java)

    getResourceId
    getProperties
    getFileFromAssets
    getFileFromRaw

##### Android之网络图片加载并实现线程切换一套解决方案：

    public static void getImageBitmap(Context context,final ImageView imageView, final String url) {
        final Handler handler = new Handler(context.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bitmap bit = (Bitmap) msg.obj;
                if(bit != null)
                    imageView.setImageBitmap(bit);
            }
        };
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                URL imgUrl = null;
                Bitmap bitmap = null;
                try {
                    imgUrl = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) imgUrl
                            .openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                    handler.sendMessage(handler.obtainMessage(0,bitmap));
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


## Contact Me

- Github: github.com/cheng2016
- Email: mitnick.cheng@outlook.com
- QQ: 1102743539


# License

    Copyright 2018 cheng2016,Inc.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
