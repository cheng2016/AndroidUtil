package io.github.cheng2016.androidutil.core

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan

/**
 * 链式构建 [SpannableStringBuilder]，少写样板 span 代码。
 *
 * ```
 * val text = TextSpans.build {
 *   append("普通")
 *   bold("加粗")
 *   color(0xFFFF0000.toInt(), "红色")
 * }
 * ```
 */
class TextSpans private constructor(
    private val builder: SpannableStringBuilder = SpannableStringBuilder(),
) {
    fun append(text: CharSequence): TextSpans {
        builder.append(text)
        return this
    }

    fun bold(text: CharSequence): TextSpans = span(text, StyleSpan(Typeface.BOLD))

    fun italic(text: CharSequence): TextSpans = span(text, StyleSpan(Typeface.ITALIC))

    fun underline(text: CharSequence): TextSpans = span(text, UnderlineSpan())

    fun color(
        color: Int,
        text: CharSequence,
    ): TextSpans = span(text, ForegroundColorSpan(color))

    fun size(
        relative: Float,
        text: CharSequence,
    ): TextSpans = span(text, RelativeSizeSpan(relative))

    fun span(
        text: CharSequence,
        what: Any,
    ): TextSpans {
        val start = builder.length
        builder.append(text)
        builder.setSpan(what, start, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return this
    }

    fun build(): CharSequence = builder

    override fun toString(): String = builder.toString()

    companion object {
        @JvmStatic
        fun create(): TextSpans = TextSpans()

        @JvmStatic
        fun build(block: TextSpans.() -> Unit): CharSequence = create().apply(block).build()
    }
}
