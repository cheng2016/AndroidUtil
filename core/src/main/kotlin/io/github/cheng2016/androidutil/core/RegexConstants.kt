package io.github.cheng2016.androidutil.core

/**
 * Common regex patterns used by [RegexUtils].
 */
object RegexConstants {
    /** Mobile number (simple): 1 + 10 digits. */
    const val MOBILE_SIMPLE = "^[1]\\d{10}$"

    /**
     * Mobile number (exact carrier prefixes, historical China mainland set).
     */
    const val MOBILE_EXACT =
        "^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,1,3,5-8])|(18[0-9])|(147))\\d{8}$"

    /** Landline telephone. */
    const val TEL = "^0\\d{2,3}[- ]?\\d{7,8}"

    /** 15-digit Chinese ID card. */
    const val ID_CARD15 =
        "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$"

    /** 18-digit Chinese ID card. */
    const val ID_CARD18 =
        "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9Xx])$"

    /** Email. */
    const val EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$"

    /** URL with scheme. */
    const val URL = "[a-zA-z]+://[^\\s]*"

    /** Chinese characters only. */
    const val ZH = "^[\\u4e00-\\u9fa5]+$"

    /**
     * Username: a-z A-Z 0-9 _ or Chinese, 6–20 chars, must not end with `_`.
     */
    const val USERNAME = "^[\\w\\u4e00-\\u9fa5]{6,20}(?<!_)$"

    /** `yyyy-MM-dd` including leap-year Feb 29. */
    const val DATE =
        "^(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|" +
            "(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|" +
            "(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)$"

    /** IPv4. */
    const val IP =
        "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)"

    /** Simplified plate-number style pattern (legacy). */
    const val CAR_ID = "\\d{5}$|[A-Z]{1}\\d{4}|\\d{1}[A-Z]{1}\\d{3}|[A-Z]{2}\\d{3}$"
}
