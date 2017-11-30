package tech.ketc.numeri.domain.twitter.model

import android.content.Intent
import android.net.Uri

val Tweet.link: String
    get() = "https://twitter.com/${user.screenName}/status/$id"

fun Variant.toIntent(): Intent {
    return Intent(Intent.ACTION_VIEW, Uri.parse(url))
}

fun TwitterUser.getIconUrl(useOrig: Boolean) = if (useOrig) originalIconUrl else iconUrl
