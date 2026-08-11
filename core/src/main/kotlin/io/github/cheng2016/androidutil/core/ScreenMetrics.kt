package io.github.cheng2016.androidutil.core

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import androidx.core.hardware.display.DisplayManagerCompat
import kotlin.math.roundToInt

/**
 * 屏幕与密度工具：dp/sp/px 换算、屏宽高、深色模式判断。
 */
object ScreenMetrics {
    @JvmStatic
    fun dpToPx(
        context: Context,
        dp: Float,
    ): Float =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics,
        )

    @JvmStatic
    fun spToPx(
        context: Context,
        sp: Float,
    ): Float =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics,
        )

    @JvmStatic
    fun pxToDp(
        context: Context,
        px: Float,
    ): Float = px / context.resources.displayMetrics.density

    @JvmStatic
    fun pxToSp(
        context: Context,
        px: Float,
    ): Float {
        val scaled =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // scaledDensity deprecated; approximate via fontScale * density
                context.resources.configuration.fontScale * context.resources.displayMetrics.density
            } else {
                @Suppress("DEPRECATION")
                context.resources.displayMetrics.scaledDensity
            }
        return px / scaled
    }

    @JvmStatic
    fun dpToPxInt(
        context: Context,
        dp: Float,
    ): Int = dpToPx(context, dp).roundToInt()

    /** 屏幕宽度（像素）。 */
    @JvmStatic
    fun widthPx(context: Context): Int = size(context).first

    /** 屏幕高度（像素）。 */
    @JvmStatic
    fun heightPx(context: Context): Int = size(context).second

    /** 是否处于深色模式（Configuration.UI_MODE_NIGHT_YES）。 */
    @JvmStatic
    fun isDarkMode(context: Context): Boolean {
        val mode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    /** densityDpi。 */
    @JvmStatic
    fun densityDpi(context: Context): Int = context.resources.displayMetrics.densityDpi

    private fun size(context: Context): Pair<Int, Int> {
        val app = context.applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wm = app.getSystemService(WindowManager::class.java)
            val bounds = wm?.currentWindowMetrics?.bounds
            if (bounds != null) {
                return bounds.width() to bounds.height()
            }
        }
        val display =
            DisplayManagerCompat.getInstance(app).getDisplay(android.view.Display.DEFAULT_DISPLAY)
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        display?.getRealMetrics(metrics)
        if (metrics.widthPixels > 0) {
            return metrics.widthPixels to metrics.heightPixels
        }
        val dm = app.resources.displayMetrics
        return dm.widthPixels to dm.heightPixels
    }
}

/**
 * Kotlin 风格扩展，便于在 UI 代码中书写。
 */
fun Context.dp(value: Float): Float = ScreenMetrics.dpToPx(this, value)

fun Context.dp(value: Int): Int = ScreenMetrics.dpToPxInt(this, value.toFloat())

fun Context.sp(value: Float): Float = ScreenMetrics.spToPx(this, value)
