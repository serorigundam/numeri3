package tech.ketc.numeri.domain.twitter.model

import android.content.Intent
import android.net.Uri

fun Variant.toIntent(): Intent {
    return Intent(Intent.ACTION_VIEW, Uri.parse(url))
}