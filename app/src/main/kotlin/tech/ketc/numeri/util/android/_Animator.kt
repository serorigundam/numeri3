package tech.ketc.numeri.util.android

import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.AlphaAnimation

private val mDuration = 300L

fun ViewPropertyAnimator.fadeIn(): ViewPropertyAnimator {
    duration = mDuration
    alpha(1f)
    alphaBy(0f)
    return this
}

fun ViewPropertyAnimator.fadeOut(): ViewPropertyAnimator {
    duration = mDuration
    alpha(0f)
    alphaBy(1f)
    return this
}

private val init: AlphaAnimation.() -> Unit = {
    duration = mDuration
    fillAfter = true
}

fun View.fadeIn() = startAnimation(AlphaAnimation(0f, 1f).apply(init))

fun View.fadeOut() = startAnimation(AlphaAnimation(1f, 0f).apply(init))