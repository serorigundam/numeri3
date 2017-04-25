package net.ketc.numeri.presentation.view.activity

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import net.ketc.numeri.R
import net.ketc.numeri.presentation.presenter.activity.MediaPresenter
import net.ketc.numeri.presentation.view.activity.ui.IMediaActivityUI
import net.ketc.numeri.presentation.view.activity.ui.MediaActivityUI
import net.ketc.numeri.util.log.v
import org.jetbrains.anko.find
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.startActivity

class MediaActivity
    : ApplicationActivity<MediaPresenter>(),
        MediaActivityInterface,
        IMediaActivityUI by MediaActivityUI() {

    override val ctx = this
    override val presenter = MediaPresenter(this)

    private var systemUiIsVisible = true

    private val hideSystemUIFunc: () -> Unit = {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private val showSystemUIFunc: () -> Unit = {
        supportActionBar!!.show()
    }

    private val hideFunc: () -> Unit = {
        hide()
    }

    private val hideHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        find<Button>(R.id.test_media).setOnClickListener {
            toggle()
            v("FullScreen", "toggle()")
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        delayedHide(500L)
    }

    private fun hide() {
        supportActionBar!!.hide()
        systemUiIsVisible = false
        hideHandler.removeCallbacks(showSystemUIFunc)
        hideHandler.postDelayed(hideSystemUIFunc, UI_ANIMATION_DELAY)
    }

    private fun show() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        systemUiIsVisible = true
        hideHandler.removeCallbacks(hideSystemUIFunc)
        hideHandler.postDelayed(showSystemUIFunc, UI_ANIMATION_DELAY)
    }

    private fun toggle() {
        if (systemUiIsVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun delayedHide(delayMills: Long = AUTO_HIDE_DELAY_MILLIS) {
        hideHandler.removeCallbacks(hideFunc)
        hideHandler.postDelayed(hideFunc, delayMills)
    }

    companion object {
        private val AUTO_HIDE_DELAY_MILLIS = 2000L
        private val UI_ANIMATION_DELAY = 200L
        fun start(ctx: Context) {
            ctx.startActivity<MediaActivity>()
        }
    }
}


interface MediaActivityInterface : ActivityInterface {

}
