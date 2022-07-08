package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Created by: cheng
 * @创建时间: 2022/7/7 18:28
 * @描述:拦截Activity的启动流程绕过AndroidManifest检测
 */
public class HookStartActivityUtil {

    private static final String TAG = "HookStartActivityUtil";
    public String EXTRA_ORIGIN_INTENT = "EXTRA_ORIGIN_INTENT";

    Context mContext;
    Class<?> mProxyclass;

    public HookStartActivityUtil(Context mContext, Class<?> mProxyclass) {
        this.mContext = mContext;
        this.mProxyclass = mProxyclass;
    }

    public  void hookLaunchActivity() throws Exception{
        Class<?> atClass = Class.forName("android.app.ActivityThread");
        Field satField = atClass.getDeclaredField("sCurrentActivityThread");
        satField.setAccessible(true);
        Object sCurrentActivityThread = satField.get(null);
        Field mhField = atClass.getDeclaredField("mH");
        mhField.setAccessible(true);
        Object mHandler =  mhField.get(sCurrentActivityThread);
        Class<?> handlerClass = Class.forName("android.os.Handler");
        Field mCallbackField = handlerClass.getDeclaredField("mCallback");
        mCallbackField.setAccessible(true);
        mCallbackField.set(mHandler,new HandlerCallBack());
    }

    private class HandlerCallBack implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == 100){
                handleLaunchActivity(msg);
            }
            return false;
        }
    }

    private void  handleLaunchActivity(Message msg){
            Object record = msg.obj;
       try {
           Field intentField = record.getClass().getDeclaredField("intent");
           intentField.setAccessible(true);
           Intent safeIntent = (Intent) intentField.get(record);
           Intent originIntetnt = safeIntent.getParcelableExtra(EXTRA_ORIGIN_INTENT);
           if(originIntetnt!=null)
           {
               intentField.set(record,originIntetnt);
           }
       } catch (Exception e) {
           e.printStackTrace();
       }

   }


    public void hookStartActivity() throws Exception {
        Class<?> amnclass = Class.forName("android.app.ActivityManagerNative");
        Field gDefaultField = amnclass.getDeclaredField("gDefault");
        gDefaultField.setAccessible(true);
        Log.e(TAG, "" + gDefaultField.getName());

        Object gDefault = gDefaultField.get(null);
        //3.2获取gDefault中的mInstance属性
        Class<?> singletonclass = Class.forName("android.util.Singleton");
        Field mInstanceField = singletonclass.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);
        Log.e(TAG, "" + mInstanceField.getName());

        Object iamInstance = mInstanceField.get(gDefault);

        Log.e(TAG, "" + iamInstance.getClass().getSimpleName());
        Class<?> iamclass = Class.forName("android.app.IActivityManager");
        iamInstance = Proxy.newProxyInstance(HookStartActivityUtil.class.getClassLoader(), new Class[]{iamclass}, new startActivityInvocationHandler(iamInstance));
        mInstanceField.set(gDefault,iamInstance);

    }

    private class startActivityInvocationHandler implements InvocationHandler {
        //方法执行者
        private Object mobject;

        public startActivityInvocationHandler(Object object) {
            this.mobject = object;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Log.e(TAG, "" + method.getName());
            //替换Intent,过AndroidManifest.xml检测
            if (method.getName().equals("startActivity")) {
                //1.首先获取原来的工ntent
                Intent originIntent = (Intent) args[2];
                //2.创建一个安全的
                Intent safeIntent = new Intent(mContext, mProxyclass);
                args[2] = safeIntent;
                //3.绑定原来的intent
                safeIntent.putExtra(EXTRA_ORIGIN_INTENT, originIntent);
            }
            return method.invoke(mobject, args);
        }
    }
}
