package io.github.cheng2016.androidutil.core

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * 软键盘（IME）显隐辅助，不持有 Activity 强引用之外的状态。
 */
object SoftInput {
    /** 对 [view] 弹出软键盘（会先 `requestFocus`）。 */
    @JvmStatic
    fun show(view: View) {
        view.requestFocus()
        imm(view.context)?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    /** 隐藏与 [view] 窗口绑定的软键盘。 */
    @JvmStatic
    fun hide(view: View) {
        imm(view.context)?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /** 隐藏当前 Activity 焦点视图上的软键盘。 */
    @JvmStatic
    fun hide(activity: Activity) {
        val view = activity.currentFocus ?: activity.window.decorView
        hide(view)
    }

    /**
     * 切换软键盘显隐。
     *
     * 底层 [InputMethodManager.toggleSoftInput] 在较新平台标记为 deprecated，
     * 仍保留作为兼容入口；优先使用 [show] / [hide]。
     */
    @JvmStatic
    @Suppress("DEPRECATION")
    fun toggle(context: Context) {
        imm(context)?.toggleSoftInput(0, 0)
    }

    private fun imm(context: Context): InputMethodManager? =
        context.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
}
