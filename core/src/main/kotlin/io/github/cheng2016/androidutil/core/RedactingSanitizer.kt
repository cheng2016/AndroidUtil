package io.github.cheng2016.androidutil.core

/**
 * Built-in [AndroidUtilLogger.Sanitizer] that redacts common credential patterns
 * (password / token / authorization / cookie) in log messages.
 */
object RedactingSanitizer : AndroidUtilLogger.Sanitizer {
    private val patterns =
        listOf(
            Regex("""(?i)(password|passwd|pwd)\s*[=:]\s*\S+"""),
            Regex("""(?i)(token|access_token|refresh_token|api[_-]?key)\s*[=:]\s*\S+"""),
            Regex("""(?i)(authorization)\s*[=:]\s*.+"""),
            Regex("""(?i)(cookie)\s*[=:]\s*.+"""),
            Regex("""(?i)bearer\s+[A-Za-z0-9\-._~+/]+=*"""),
        )

    override fun sanitize(
        level: AndroidUtilLogger.Level,
        tag: String,
        message: String,
    ): String {
        var out = message
        for (pattern in patterns) {
            out =
                pattern.replace(out) { match ->
                    val raw = match.value
                    val idx = raw.indexOfFirst { it == '=' || it == ':' }
                    if (idx >= 0) {
                        raw.substring(0, idx + 1) + " ***"
                    } else {
                        "***"
                    }
                }
        }
        return out
    }
}
