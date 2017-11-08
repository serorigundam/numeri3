package tech.ketc.numeri.util.android

import android.view.ViewPropertyAnimator

private val duration = 300L

fun ViewPropertyAnimator.fadeIn(): ViewPropertyAnimator {
    duration = 300
    alpha(1f)
    alphaBy(0f)
    return this
}

fun ViewPropertyAnimator.fadeOut(): ViewPropertyAnimator {
    duration = 300
    alpha(0f)
    alphaBy(1f)
    return this
}

