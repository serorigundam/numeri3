package net.ketc.numeri.util.android

import android.view.View
import android.view.animation.AlphaAnimation


fun View.fadeIn() {
    val anim = AlphaAnimation(0f, 1f)
    anim.duration = 50
    this.startAnimation(anim)
}

fun View.fadeOut() {
    val anim = AlphaAnimation(1f, 0f)
    anim.duration = 50
    this.startAnimation(anim)
}