import android.content.Context;

import com.icloud.sdk.view.LoadingBar;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

/**
 * android-async-http 库的工具类
 * 
 * 依赖 httpclient-4.3.6、android-async-http-1.4.9
 */

public class HttpUtil {
    private static LoadingBar loadingBar;
    private static AsyncHttpClient client = new AsyncHttpClient();
    private static boolean isSetLoading = false;

    public static AsyncHttpClient getClient(){
        client.setTimeout(20000);
        return client;
    }

    public static void init(){
        client.setTimeout(20000);
    }
    
    public static void post(Context ctx, String url, String jsonStr, SimpleResponseHandler responseHandler ){
        isSetLoading= true;
        loadingBar = new LoadingBar(ctx);
        LogUtil.d("post","url:"+url+"\njsonStr:"+jsonStr);
        client.post(ctx,url,Util.addEntity(jsonStr),null,responseHandler);
    }

    public static void postNoLoading(Context ctx, String url, String jsonStr, SimpleResponseHandler responseHandler ){
        isSetLoading= false;
        LogUtil.d("post","url:"+url+"\njsonStr:"+jsonStr);
        client.post(ctx,url,Util.addEntity(jsonStr),null,responseHandler);
    }
    
    public static abstract class SimpleResponseHandler extends AsyncHttpResponseHandler{
        @Override
        public void onStart() {
           if (isSetLoading){
               loadingBar.show();
           }
        }

        @Override
        public void onFinish() {
            if (isSetLoading){
                loadingBar.cancel();
            }
        }


        @Override
        public void onSuccess(int i, Header[] headers, byte[] bytes) {
            LogUtil.d("response onSuccess","response:"+new String(bytes));
        }


        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            LogUtil.d("response onFailure","response:"+new String(bytes));
        }
    }
}
