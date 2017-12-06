package tech.ketc.numeri.util.twitter4j

import android.content.Context
import android.support.v4.app.Fragment
import org.jetbrains.anko.toast
import tech.ketc.numeri.R
import twitter4j.TwitterException

val TwitterException.errorMessageId: Int
    get() = when (errorCode) {
        32 -> R.string.twitter_error_32
        34 -> R.string.twitter_error_34
        36 -> R.string.twitter_error_36
        50 -> R.string.twitter_error_50
        63 -> R.string.twitter_error_63
        64 -> R.string.twitter_error_64
        88 -> R.string.twitter_error_88
        130 -> R.string.twitter_error_130
        144 -> R.string.twitter_error_144
        179 -> R.string.twitter_error_179
        185 -> R.string.twitter_error_185
        187 -> R.string.twitter_error_187
        else -> R.string.twitter_error_unknown
    }

fun Fragment.showTwitterError(throwable: Throwable) {
    context?.showTwitterError(throwable)
}

fun Context.showTwitterError(throwable: Throwable) {
    (throwable as? TwitterException)?.let {
        val errorMessageId = it.errorMessageId
        when (errorMessageId) {
            R.string.twitter_error_unknown -> {
                toast(getString(errorMessageId) + " code: ${it.errorCode}")
            }
            else -> toast(it.errorMessageId)
        }
    }
}