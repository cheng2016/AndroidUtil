import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;

import android.os.Looper;

/**
 * @ClassName ImageUtils
 * @Description 三级缓存的图片加载库，代码短效精悍，使用于代码低耦合的sdk场景
 * @Author chengzj
 * @Date 2021/8/10 9:55
 */
public class ImageDownLoader {
    public static final String TAG = ImageDownLoader.class.getSimpleName();
    private ImageCache mMemoryCache;

    private static ImageDownLoader instance;

    public static ImageDownLoader getInstance(Context paramContext) {
        if (instance == null) {
            synchronized (ImageDownLoader.class) {
                instance = new ImageDownLoader(paramContext);
            }
        }
        return instance;
    }

    private ImageDownLoader(Context paramContext) {
        if (paramContext == null) return;
        mMemoryCache = new ImageCache(paramContext);
    }

    private void downloadImage(final ImageView imageView, final String url) {
        Log.d(TAG, "网络加载：" + url);
        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bitmap bit = (Bitmap) msg.obj;
                if (bit != null)
                    imageView.setImageBitmap(bit);
            }
        };
        Executors.newFixedThreadPool(2).execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = null;
                URL imgUrl = null;
                try {
                    imgUrl = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) imgUrl
                            .openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                    addBitmapToMemoryCache(url, bitmap);
                    mMemoryCache.putDiskCache(bitmap, url);
                    handler.sendMessage(handler.obtainMessage(0, bitmap));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void addBitmapToMemoryCache(String paramString, Bitmap paramBitmap) {
        if (getBitmapFromMemCache(paramString) == null && paramBitmap != null)
            this.mMemoryCache.put(md5(paramString), paramBitmap);
    }

    private Bitmap getBitmapFromMemCache(String paramString) {
        paramString = md5(paramString);
        //先在强引用中查找
        Bitmap bitmap = mMemoryCache.get(paramString);
        if (bitmap == null) {
            // 如果图片不存在强引用中，则去软引用（SoftReference）中查找
            SoftReference<Bitmap> softReference = mMemoryCache.getCacheMap().get(paramString);
            if (softReference != null) {
                bitmap = softReference.get();
                mMemoryCache.put(paramString, bitmap);
            }
        }
        return bitmap;
    }

    public void load(ImageView imageView, String url) {
        if (TextUtils.isEmpty(url)) return;
        Bitmap bitmap = getBitmapFromMemCache(url);
        if (bitmap != null) {
            Log.d(TAG, "缓存加载：" + url);
            imageView.setImageBitmap(bitmap);
            return;
        }

        bitmap = mMemoryCache.getDiskCache(url);
        if (bitmap != null) {
            Log.d(TAG, "磁盘加载：" + url);
            addBitmapToMemoryCache(url, bitmap);
            imageView.setImageBitmap(bitmap);
            return;
        }
        downloadImage(imageView, url);
    }

    public class ImageCache extends LruCache<String, Bitmap> {
        private Map<String, SoftReference<Bitmap>> cacheMap;
        private String diskpath;
        private boolean hasPermissions = false;

        public ImageCache() {
            super((int) (Runtime.getRuntime().maxMemory() / 10));
            cacheMap = new HashMap<>();
        }

        public ImageCache(Context context) {
            this();
            if (!lacksPermissions(context, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                hasPermissions = true;
                Log.e(TAG, "拥有读写权限 可以进行磁盘缓存 hasPermissions is true");
            }
            diskpath = getDiskCacheDir(context, "bitmap");
        }


        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }

        @Override
        protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
            if (oldValue != null) {
                SoftReference<Bitmap> softReference = new SoftReference<Bitmap>(oldValue);
                cacheMap.put(key, softReference);
            }
        }

        public Map<String, SoftReference<Bitmap>> getCacheMap() {
            return cacheMap;
        }

        private void putDiskCache(Bitmap paramBitmap, String paramString) {
            if (getDiskCache(paramString) != null || !hasPermissions) return;
            try {
                if (paramString.endsWith(".jpg")) {
                    paramString = diskpath + File.separator + md5(paramString) + ".jpg";
                } else if (paramString.endsWith(".png")) {
                    paramString = diskpath + File.separator + md5(paramString) + "png";
                }
                File file = new File(paramString);
                if (file.exists() ? file.createNewFile() : (file.getParentFile().exists() ? file.createNewFile() : file.getParentFile().mkdirs())){
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    paramBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    Log.i(TAG, "写入磁盘中 " + paramString);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private Bitmap getDiskCache(String paramString) {
            if (!hasPermissions) return null;
            if (paramString.endsWith(".jpg")) {
                paramString = diskpath + File.separator + md5(paramString) + ".jpg";
            } else if (paramString.endsWith(".png")) {
                paramString = diskpath + File.separator + md5(paramString) + "png";
            }
            if (new File(paramString).exists()) {
                return BitmapFactory.decodeFile(paramString);
            }
            return null;
        }
    }


    private String md5(String content) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(content.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException("NoSuchAlgorithmException", e);
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append(0);
            }
            hex.append(Integer.toHexString(b & 0xff));
        }
        return hex.toString();
    }

    private String getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = Environment.getExternalStorageDirectory() + File.separator + context.getPackageName();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        Log.d(TAG, "磁盘图片缓存目录 " + cachePath + File.separator + uniqueName);
        return cachePath + File.separator + uniqueName;
    }

    // return true-表示没有权限  false-表示权限已开启
    private static boolean lacksPermissions(Context mContexts, String... args) {
        for (String permission : args) {
            if (lacksPermission(mContexts, permission)) {
                return true;
            }
        }
        return false;
    }

    private static boolean lacksPermission(Context context, String permission) {
        return checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_DENIED;
    }

    private static int checkSelfPermission(Context context, String permission) {
        if (permission == null) {
            throw new IllegalArgumentException("permission is null");
        }
        return context.checkPermission(permission, android.os.Process.myPid(), Process.myUid());
    }
}
