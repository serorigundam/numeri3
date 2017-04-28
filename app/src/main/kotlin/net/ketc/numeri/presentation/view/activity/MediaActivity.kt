package net.ketc.numeri.presentation.view.activity

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import net.ketc.numeri.R
import net.ketc.numeri.domain.model.MediaEntity
import net.ketc.numeri.domain.model.MediaType
import net.ketc.numeri.presentation.presenter.activity.MediaPresenter
import net.ketc.numeri.presentation.view.activity.ui.IMediaActivityUI
import net.ketc.numeri.presentation.view.activity.ui.MediaActivityUI
import net.ketc.numeri.presentation.view.component.adapter.SimplePagerAdapter
import net.ketc.numeri.presentation.view.fragment.ImageMediaFragment
import net.ketc.numeri.presentation.view.fragment.ImageMediaFragmentInterface
import net.ketc.numeri.presentation.view.fragment.MovieMediaFragment
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

    private val adapter: SimplePagerAdapter by lazy { createAdapter() }

    private var visiblePosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState?.let { visiblePosition = it.getInt(EXTRA_POSITION) } == null) {
            visiblePosition = intent.getIntExtra(EXTRA_POSITION, 0)
        }
        initializePager()
    }

    fun createAdapter(): SimplePagerAdapter {
        fun Any?.asMediaEntity(): MediaEntity {
            return takeIf { it is MediaEntity }
                    ?.let { it as MediaEntity }
                    ?: throw IllegalStateException("Invalid key value of EXTRA_MEDIA_ENTITIES")
        }

        fun MediaEntity.toFragment(): Fragment = when (type) {
            MediaType.PHOTO -> ImageMediaFragment.create(this)
            MediaType.ANIMATED_GIF, MediaType.VIDEO -> MovieMediaFragment.create(this)
        }

        return (intent.getSerializableExtra(EXTRA_MEDIA_ENTITIES) as Array<*>)
                .map(Any?::asMediaEntity)
                .map(MediaEntity::toFragment)
                .let { SimplePagerAdapter(supportFragmentManager, it) }
    }

    fun initializePager() {
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                visiblePosition = position
            }
        })
        val position = visiblePosition
        pager.adapter = adapter
        pager.currentItem = position
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_media, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_save).isVisible = adapter.getItem(visiblePosition) is ImageMediaFragmentInterface
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                (adapter.getItem(visiblePosition) as? ImageMediaFragmentInterface)?.saveImage()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
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

    override fun toggle() {
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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(EXTRA_POSITION, visiblePosition)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private val AUTO_HIDE_DELAY_MILLIS = 2000L
        private val UI_ANIMATION_DELAY = 200L
        private val EXTRA_MEDIA_ENTITIES = "EXTRA_MEDIA_ENTITIES"
        private val EXTRA_POSITION = "EXTRA_POSITION"

        fun start(ctx: Context, mediaEntities: List<MediaEntity>, position: Int = 0) {
            ctx.startActivity<MediaActivity>(EXTRA_MEDIA_ENTITIES to mediaEntities.toTypedArray(),
                    EXTRA_POSITION to position)
        }
    }
}


interface MediaActivityInterface : ActivityInterface {
    fun toggle()
}
