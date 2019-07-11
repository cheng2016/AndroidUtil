import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;
import com.icloud.sdk.model.PayResult;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;


/**
 *
 * 阿里巴巴支付工具类
 *
 */
public class AlipayUtil {
    public interface CallbackListener {
         void onResult(ResultCode code, String msg, String descr);
    }
    public enum  ResultCode {
        SUCCESS,
        CANCEL,
        Fail,
    }
    
    private static final int SDK_PAY_FLAG = 1;

    private static AlipayUtil instance;

    public static AlipayUtil instance() {
        if (instance == null) {
            instance = new AlipayUtil();
        }
        return instance;
    }
    
    CallbackListener payListener;
    public void pay(final Activity ctx, JSONObject jsonObject,CallbackListener listener) {
        this.payListener = listener;
        String orderInfo = jsonObject.optString("orderInfo");
        String sign = jsonObject.optString("sign");
        try {
            // 仅需对sign 做URL编码
            sign = URLEncoder.encode(sign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 完整的符合支付宝参数规范的订单信息
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
                + getSignType();
                
        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(ctx);
                // 调用支付接口，获取支付结果
                Map<String, String> result = alipay.payV2(payInfo, true);

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }        
                
    /**
     * get the sign type we use. 获取签名方式
     */
    private String getSignType() {
        return "sign_type=\"RSA\"";
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    if (payListener != null) { 
                        PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                        // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                        String resultInfo = payResult.getResult();
                        String resultStatus = payResult.getResultStatus();
                        // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                        if (TextUtils.equals(resultStatus, "9000")) {
                            payListener.onResult(ResultCode.SUCCESS, "","");
                        } else {
                            payListener.onResult(ResultCode.Fail, "","");

                        }
                    }
                    break;
                }           
                default:
                    break;
            }
    };
    
  public class PayResult {
    private String resultStatus;
    private String result;
    private String memo;
    
    public PayResult(Map<String, String> rawResult) {
        if (rawResult == null) {
            return;
        }

        for (String key : rawResult.keySet()) {
            if (TextUtils.equals(key, "resultStatus")) {
              resultStatus = rawResult.get(key);
            } else if (TextUtils.equals(key, "result")) {
              result = rawResult.get(key);
            } else if (TextUtils.equals(key, "memo")) {
              memo = rawResult.get(key);
            }
        }
	  }
    
    @Override
    public String toString() {
        return "resultStatus={" + resultStatus + "};memo={" + memo
            + "};result={" + result + "}";
    }

    /**
     * @return the resultStatus
     */
    public String getResultStatus() {
        return resultStatus;
    }

    /**
     * @return the memo
     */
    public String getMemo() {
        return memo;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }  
  
}
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
