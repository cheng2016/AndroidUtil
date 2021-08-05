import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.Executors;


/**
 * @author cheng
 * 文件名：HttpClientUtils
 * 创建日期：2020/5/21 18:15
 * 描述：HttpURLConnection 的请求工具类，封装主线程子线程切换
 */
public class HttpClientUtils {
    public static final String TAG = HttpClientUtils.class.getSimpleName();

    public static void post(final String actionUrl, final Map<String, String> params, final Callback callback) {
        HyLog.d(TAG, "post Url : " + actionUrl);
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                String result = null;
                try {
                    URL uri = new URL(actionUrl);
                    HttpURLConnection conn = getPostHttpURLConnection(uri);
                    StringBuilder sb = getPostStringBuilder(params);
                    OutputStream output = conn.getOutputStream();
                    output.write(sb.toString().getBytes());
                    output.flush();
                    int res = conn.getResponseCode();
                    InputStream in = null;
                    switch (res) {
                        case 200:
                            result = executeData(conn, in);
//                            HyLog.i(TAG," result : " + result);
                            callback.onSuccess(result);
                            break;
                        case 403:
                            callback.onFailure(new IOException(res + "403 : request forbided"));
                        case 404:
                            callback.onFailure(new IOException(res + "404 : not fonud such address"));
                        default:
                            callback.onFailure(new IOException(res + " : undefine error"));
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    callback.onFailure(exception);
                }
            }
        });
    }


    public static void post(final String actionUrl, final Map<String, String> params, final SimpleResponseCallback callback) {
        HyLog.d(TAG, " post Url : " + actionUrl);
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                String result = null;
                try {
                    URL uri = new URL(actionUrl);
                    HttpURLConnection conn = getPostHttpURLConnection(uri);
                    StringBuilder sb = getPostStringBuilder(params);
                    OutputStream output = conn.getOutputStream();
                    output.write(sb.toString().getBytes());
                    output.flush();
                    int res = conn.getResponseCode();
                    InputStream in = null;
                    switch (res) {
                        case 200:
                            result = executeData(conn, in);
                            System.out.println(TAG + " result : " + result);
                            callback.sendSuccessMessage(result);
                            break;
                        case 403:
                            callback.sendFailuerMessage(new IOException(res + " : request forbided"));
                        case 404:
                            callback.sendFailuerMessage(new IOException(res + " : not fonud such address"));
                        default:
                            callback.sendFailuerMessage(new IOException(res + " : undefine error"));
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    callback.sendFailuerMessage(exception);
                }
            }
        });
    }

    public static void get(final String actionUrl, final Map<String, String> params, final SimpleResponseCallback callback) {
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String contentType = "application/x-www-form-urlencoded";
                    URL uri = new URL(getRequestUrl(actionUrl, params));
                    HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
                    conn.setReadTimeout(5 * 1000); // Cache max time
                    conn.setDoInput(true);// allow input
                    conn.setDoOutput(true);// Allow output
                    conn.setUseCaches(false); // cache is disable
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("connection", "keep-alive");
                    conn.setRequestProperty("Charsert", "UTF-8");
                    conn.setRequestProperty("Content-Type", contentType);
                    int res = conn.getResponseCode();
                    InputStream in = null;
                    String result = null;
                    switch (res) {
                        case 200:
                            result = executeData(conn, in);
                            callback.sendSuccessMessage(result);
                            break;
                        case 403:
                            callback.sendFailuerMessage(new IOException(res + " : request forbided"));
                        case 404:
                            callback.sendFailuerMessage(new IOException(res + " : not fonud such address"));
                        default:
                            callback.sendFailuerMessage(new IOException(res + " : undefine error"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static String executeData(HttpURLConnection conn, InputStream in) throws Exception {
        in = conn.getInputStream();
        String line;
        StringBuilder sb2 = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        while ((line = bufferedReader.readLine()) != null) {
            sb2.append(line);
        }
        in.close();
        return sb2.toString();
    }

    public static String getRequestUrl(final String actionUrl, final Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (TextUtils.isEmpty(entry.getValue())) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue()));
        }
        HyLog.d(TAG, "get 参数 : " + sb.toString());
        final String url;
        if (actionUrl.endsWith("?")) {
            url = actionUrl + sb.toString();
        } else {
            url = actionUrl + "?" + sb.toString();
        }
        HyLog.d(TAG, " get 完整链接 : " + url);
        return url;
    }

    public static HttpURLConnection getPostHttpURLConnection(URL uri) throws Exception {
        String contentType = "application/x-www-form-urlencoded";
        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
        conn.setReadTimeout(6 * 1000); // Cache max time
        conn.setConnectTimeout(6 * 1000);
        conn.setDoInput(true);// allow input
        conn.setDoOutput(true);// Allow output
        conn.setUseCaches(false); // cache is disable
        conn.setRequestMethod("POST");
        conn.setRequestProperty("connection", "keep-alive");
        conn.setRequestProperty("Charsert", "UTF-8");
        conn.setRequestProperty("Content-Type", contentType);
        return conn;
    }

    public static StringBuilder getPostStringBuilder(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (TextUtils.isEmpty(entry.getValue())) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue()));
        }
        HyLog.d(TAG, "post params : " + sb.toString());
        return sb;
    }

    public abstract static class SimpleResponseCallback implements Callback {
        private Handler handler;

        public SimpleResponseCallback() {
            Looper looper = Looper.myLooper();
            this.handler = new ResultHandler(this, looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    Object[] objects = (Object[]) message.obj;
                    onSuccess((String) objects[0]);
                    break;
                case 1:
                    onFailure((Exception) message.obj);
                    break;
                default:
                    break;
            }
        }

        void sendSuccessMessage(String result) {
            this.handler.sendMessage(obtainMessage(0, new Object[]{result}));
        }

        void sendFailuerMessage(Throwable throwable) {
            this.handler.sendMessage(obtainMessage(1, throwable));
        }

        Message obtainMessage(int responseMessageId, Object responseMessageData) {
            return Message.obtain(this.handler, responseMessageId, responseMessageData);
        }
    }

    private static class ResultHandler extends Handler {
        SimpleResponseCallback responseHandler;

        ResultHandler(SimpleResponseCallback handler, Looper looper) {
            super(looper);
            this.responseHandler = handler;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            responseHandler.handleMessage(msg);
        }
    }

    public interface Callback {
        void onSuccess(String responseJson);

        void onFailure(Exception error);
    }
}
