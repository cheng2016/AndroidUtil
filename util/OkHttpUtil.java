
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by admin on 2019/6/14.
 * <p>
 * 模仿Async-Http通信库写出的okhttp子线程跨主线程通信类
 * <p>
 * 主要参考AsyncHttpClient、AsyncHttpResponseHandler类
 * 参考Retofit2中OkHttpCall类、okhttp3中RealCall类
 * 主要采用了适配器模式、模板模式
 * <p>
 * loadingBar 加载框
 */

public class OkHttpUtils {
    private static final String TAG = "OkHttpUtils";

    private static OkHttpClient client;

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private OkHttpUtils() {
    }

    static {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder().writeTimeout(30 * 1000, TimeUnit.MILLISECONDS)
                .readTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                .connectTimeout(15 * 1000, TimeUnit.MILLISECONDS)
                //设置拦截器，显示日志信息
                .addNetworkInterceptor(httpLoggingInterceptor)
                .build();
    }

    public static void postLoading(Context context, String url, Map<String, String> paramsMap, final SimpleResponseHandler responseHandler) {
        Logger.d("postLoading", "url:" + url + " requestData: " + Arrays.asList(paramsMap).toString());
        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
            builder.add(key, paramsMap.get(key));
        }
        responseHandler.isSetLoading = true;
        postForm(context, url, builder.build(), responseHandler);
    }

    public static void postNoLoading(String url, Map<String, String> paramsMap, final SimpleResponseHandler responseHandler) {
        Logger.d("postNoLoading", "url:" + url + " requestData: " + Arrays.asList(paramsMap).toString());
        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
            builder.add(key, paramsMap.get(key));
        }
        responseHandler.isSetLoading = false;
        postForm(url, builder.build(), responseHandler);
    }

    public static void postForm(String url, FormBody formBody, final SimpleResponseHandler responseHandler) {
        postForm(null, url, formBody, responseHandler);
    }

    public static void postForm(Context context, String url, FormBody formBody, final SimpleResponseHandler responseHandler) {
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        Call call = client.newCall(request);
        responseHandler.context = context;
        EXECUTOR_SERVICE.execute(new ResponseRunnable(call, responseHandler));
    }

    public static void postMarkdown(Context context, String url, String jsonStr, final SimpleResponseHandler responseHandler) {
        Logger.d(TAG, "post url:" + url + "\njsonStr:" + jsonStr);
        MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(mediaType, jsonStr))
                .build();
        Call call = client.newCall(request);
        responseHandler.isSetLoading = true;
        responseHandler.context = context;
        EXECUTOR_SERVICE.execute(new ResponseRunnable(call, responseHandler));
    }

    public static void postJson(Context context, String url, JSONObject jsonStr, final SimpleResponseHandler responseHandler) {
        Logger.d(TAG, "postNoLoading url:" + url + "\njsonStr:" + jsonStr);
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(mediaType, jsonStr.toString()))
                .build();
        Call call = client.newCall(request);
        responseHandler.isSetLoading = true;
        responseHandler.context = context;
        EXECUTOR_SERVICE.execute(new ResponseRunnable(call, responseHandler));
    }

    public static void get(String url, final SimpleResponseHandler responseHandler) {
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        Call call = client.newCall(request);
        EXECUTOR_SERVICE.execute(new ResponseRunnable(call, responseHandler));
    }

    public static void release() {

    }

    private static class ResponseRunnable implements Runnable {
        private Call call;
        private SimpleResponseHandler callback;

        public ResponseRunnable(Call call, SimpleResponseHandler callback) {
            this.call = call;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                callback.sendStartMessage();
                Response response = call.execute();
                callback.onResponse(call, response);
            } catch (IOException e) {
                e.printStackTrace();
                callback.onFailure(call, e);
            }
            callback.sendFinishMessage();
        }
    }

    /**
     * 模板模式-----定义算法的步骤，并把这些实现延迟到子类
     */
    public abstract static class SimpleResponseHandler implements Callback {
        private Handler handler;

        private Context context;
        private boolean isSetLoading = false;
        private LoadingBar loadingBar;

        public SimpleResponseHandler() {
            Looper looper = Looper.myLooper();
            this.handler = new ResultHandler(this, looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case -1:
                    onStart();
                    break;
                case 0:
                    Object[] objects = (Object[]) message.obj;
                    onSuccess((Call) objects[0], (Response) objects[1]);
                    break;
                case 1:
                    onFailure((Exception) message.obj);
                    break;
                case 2:
                    onFinish();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            Logger.d(TAG, "SimpleResponseHandler   onResponse current Thread: " + Thread.currentThread().getName() + " , ThreadId : " + Thread.currentThread().getId());
            if (response.code() < 200 || response.code() >= 300) {
                sendFailuerMessage(new IOException(response.message()));
            } else {
                sendSuccessMessage(response.code(), call, response);
            }
        }

        @Override
        public void onFailure(Call call, IOException e) {
            Log.e(TAG, "SimpleResponseHandler   onFailure current Thread: " + Thread.currentThread().getName() + " , ThreadId : " + Thread.currentThread().getId());
            sendFailuerMessage(e);
        }

        void sendStartMessage() {
            this.handler.sendMessage(obtainMessage(-1, null));
        }

        void sendSuccessMessage(int code, Call call, Response response) {
            this.handler.sendMessage(obtainMessage(0, new Object[]{call, response}));
        }

        void sendFailuerMessage(Throwable throwable) {
            this.handler.sendMessage(obtainMessage(1, throwable));
        }

        void sendFinishMessage() {
            this.handler.sendMessage(obtainMessage(2, null));
        }

        Message obtainMessage(int responseMessageId, Object responseMessageData) {
            return Message.obtain(this.handler, responseMessageId, responseMessageData);
        }

        public void onStart() {
            Logger.d(TAG, "SimpleResponseHandler    onStart");
            if (isSetLoading && context != null) {
                Logger.i(TAG, "SimpleResponseHandler    loadingBar show");
                loadingBar = new LoadingBar(context);
                loadingBar.show();
            }

        }

        public void onFinish() {
            Logger.d(TAG, "SimpleResponseHandler    onFinish");
            if (isSetLoading) {
                Logger.i(TAG, "SimpleResponseHandler    loadingBar hide");
                if (loadingBar != null)
                    loadingBar.cancel();
                loadingBar = null;
            }
        }

        public abstract void onSuccess(Call call, Response response);

        public abstract void onFailure(Exception error);
    }

    /**
     * 类适配器模式------将一个类的接口，转换为客户期盼的另一个接口。通过继承的方式.
     */
    private static class ResultHandler extends Handler {
        SimpleResponseHandler responseHandler;

        ResultHandler(SimpleResponseHandler handler, Looper looper) {
            super(looper);
            this.responseHandler = handler;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            responseHandler.handleMessage(msg);
        }
    }



    public class LoadingBar extends ProgressDialog {
        public LoadingBar(Context context) {
            super(context);
        }
    }
}

