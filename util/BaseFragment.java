package com.vtech.app.moudle;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vtech.app.util.Logger;

import butterknife.ButterKnife;


/**
 * Created by chengzj on 2017/6/17.
 * <p>
 * 实现懒加载的基类fragment
 */

public abstract class BaseFragment extends Fragment {
    public String TAG = "";


    private Context context;
    /**
     * 标志位：fragment是否可见
     */
    private boolean isVisible;

    /**
     * 标志位：是否已加载fragment视图
     */
    private boolean isPrepared = false;
    /**
     * 标志位：是否已进行懒加载数据，保证数据只加载一次
     */
    private boolean isLoadData = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        TAG = this.getClass().getSimpleName();
        Logger.d(TAG, "setUserVisibleHint() -> isVisibleToUser: " + isVisibleToUser);
        if (getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        Logger.d(TAG, "onAttach");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, "onCreateView");
        View view = inflater.inflate(getLayoutId(), container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.d(TAG, "onViewCreated");
        initView(view, savedInstanceState);
        isPrepared = true;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.d(TAG, "onActivityCreated");
        initData();
        onVisible();//防止viewpager加载的第一个视图时,第一个视图提前执行setUserVisibleHint方法导致懒加载无效的问题
    }

    /**
     * fragment可见时执行
     */
    private void onVisible() {
        Logger.d(TAG, "onVisible  isPrepared：" + isPrepared + "，isVisible：" + isVisible + "，isLoadData：" + isLoadData);
        if (!isPrepared || !isVisible || isLoadData) {
            return;
        }
        isLoadData = true;
        onLazyLoad();
    }

    /**
     * fragment不可见时执行,可选方法一般不用
     */
    protected void onInvisible(){}

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.d(TAG, "onActivityResult");
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.d(TAG, "onStart");
    }


    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Logger.d(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        Logger.d(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "onDestroy");
    }

    protected abstract int getLayoutId();

    protected abstract void initView(View view, Bundle savedInstanceState);

    protected abstract void initData();
    
    /**
     * 懒加载核心方法
     */
    protected abstract void onLazyLoad();
}
