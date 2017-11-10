package tech.ketc.numeri.domain.repository

val urlRegex = "https?://[\\w/:%#$&?()~.=+\\-]+".toRegex()

fun checkUrl(str: String) {
    if (!str.matches(urlRegex))
        throw IllegalArgumentException("specified string is not URL")
}
