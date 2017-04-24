package net.ketc.numeri.util.android

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation

/**
 * @param durationMillis Duration in milliseconds
 */
fun View.fadeIn(durationMillis: Long = 50L): AnimationCallback {
    val anim = AlphaAnimation(0f, 1f)
    anim.duration = durationMillis
    return AnimationCallback(anim, this)
}

/**
 * @param durationMillis Duration in milliseconds
 */
fun View.fadeOut(durationMillis: Long = 50L): AnimationCallback {
    val anim = AlphaAnimation(1f, 0f)
    anim.duration = durationMillis
    return AnimationCallback(anim, this)
}

class AnimationCallback(private val animation: Animation, private val view: View) {
    private var repeat: View.(Animation) -> Unit = {}
    private var end: View.(Animation) -> Unit = {}
    private var start: View.(Animation) -> Unit = {}
    private var then: (View.() -> Unit)? = null

    init {
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation) {
                this@AnimationCallback.repeat(view, animation)
            }

            override fun onAnimationEnd(animation: Animation) {
                this@AnimationCallback.end(view, animation)
                this@AnimationCallback.then?.invoke(view)
            }

            override fun onAnimationStart(animation: Animation) {
                this@AnimationCallback.start(view, animation)
            }
        })
    }


    infix fun repeat(func: View.(Animation) -> Unit): AnimationCallback {
        repeat = func
        return this
    }

    infix fun start(func: View.(Animation) -> Unit): AnimationCallback {
        start = func
        return this
    }

    infix fun end(func: View.(Animation) -> Unit): AnimationCallback {
        end = func
        return this
    }

    fun execute() {
        view.startAnimation(animation)
    }

    infix fun then(func: View.() -> Unit): Unit {
        then = func
        execute()
    }
}