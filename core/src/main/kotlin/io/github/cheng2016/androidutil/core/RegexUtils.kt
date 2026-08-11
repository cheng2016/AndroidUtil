package io.github.cheng2016.androidutil.core

import java.util.regex.Pattern

/**
 * Regex validation and extraction helpers (Kotlin-first, Java-friendly).
 */
object RegexUtils {
    @JvmStatic
    fun isMobileSimple(input: CharSequence?): Boolean = isMatch(RegexConstants.MOBILE_SIMPLE, input)

    @JvmStatic
    fun isMobileExact(input: CharSequence?): Boolean = isMatch(RegexConstants.MOBILE_EXACT, input)

    @JvmStatic
    fun isTelephone(input: CharSequence?): Boolean = isMatch(RegexConstants.TEL, input)

    @JvmStatic
    fun isCarId(input: CharSequence?): Boolean = isMatch(RegexConstants.CAR_ID, input)

    @JvmStatic
    fun isIdCard15(input: CharSequence?): Boolean = isMatch(RegexConstants.ID_CARD15, input)

    @JvmStatic
    fun isIdCard18(input: CharSequence?): Boolean = isMatch(RegexConstants.ID_CARD18, input)

    @JvmStatic
    fun isEmail(input: CharSequence?): Boolean = isMatch(RegexConstants.EMAIL, input)

    @JvmStatic
    fun isUrl(input: CharSequence?): Boolean = isMatch(RegexConstants.URL, input)

    @JvmStatic
    fun isZh(input: CharSequence?): Boolean = isMatch(RegexConstants.ZH, input)

    @JvmStatic
    fun isUsername(input: CharSequence?): Boolean = isMatch(RegexConstants.USERNAME, input)

    @JvmStatic
    fun isDate(input: CharSequence?): Boolean = isMatch(RegexConstants.DATE, input)

    @JvmStatic
    fun isIp(input: CharSequence?): Boolean = isMatch(RegexConstants.IP, input)

    /**
     * Returns whether [input] fully matches [regex].
     */
    @JvmStatic
    fun isMatch(
        regex: String,
        input: CharSequence?,
    ): Boolean {
        if (input == null || input.isEmpty()) return false
        return Pattern.matches(regex, input)
    }

    /**
     * All substrings matching [regex], or an empty list when [input] is null.
     */
    @JvmStatic
    fun getMatches(
        regex: String,
        input: CharSequence?,
    ): List<String> {
        if (input == null) return emptyList()
        val matches = ArrayList<String>()
        val matcher = Pattern.compile(regex).matcher(input)
        while (matcher.find()) {
            matches.add(matcher.group())
        }
        return matches
    }

    /**
     * Splits [input] by [regex], or empty array when [input] is null.
     */
    @JvmStatic
    fun getSplits(
        input: String?,
        regex: String,
    ): Array<String> {
        if (input == null) return emptyArray()
        return input.split(regex.toRegex()).toTypedArray()
    }

    @JvmStatic
    fun replaceFirst(
        input: String?,
        regex: String,
        replacement: String,
    ): String? {
        if (input == null) return null
        return Pattern.compile(regex).matcher(input).replaceFirst(replacement)
    }

    @JvmStatic
    fun replaceAll(
        input: String?,
        regex: String,
        replacement: String,
    ): String? {
        if (input == null) return null
        return Pattern.compile(regex).matcher(input).replaceAll(replacement)
    }
}
