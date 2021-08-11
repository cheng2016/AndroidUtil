import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

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

import android.os.Looper;

/**
 * @ClassName ImageUtils
 * @Description 二级缓存的图片加载库，使用于小场面
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
        if (paramContext == null)
            return;
        mMemoryCache = new ImageCache();
    }

    private void downloadImage(final ImageView imageView, final String url) {
        Log.d(TAG,"网络加载：" + url);
        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bitmap bit = (Bitmap) msg.obj;
                if (bit != null)
                    imageView.setImageBitmap(bit);
            }
        };
        Executors.newSingleThreadExecutor().execute(new Runnable() {
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
                    handler.sendMessage(handler.obtainMessage(0, bitmap));
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
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
            Log.d(TAG,"缓存加载：" + url);
            imageView.setImageBitmap(bitmap);
            return;
        }
        downloadImage(imageView, url);
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

    public class ImageCache extends LruCache<String, Bitmap> {
        private Map<String, SoftReference<Bitmap>> cacheMap;

        public ImageCache() {
            super((int) (Runtime.getRuntime().maxMemory() / 10));
            cacheMap = new HashMap<>();
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
    }
}
