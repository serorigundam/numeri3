package tech.ketc.numeri.ui.activity.media

import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import tech.ketc.numeri.domain.twitter.model.MediaEntity
import tech.ketc.numeri.ui.model.MediaViewModel
import tech.ketc.numeri.util.android.fadeIn
import tech.ketc.numeri.util.android.fadeOut
import tech.ketc.numeri.util.android.setUpSupportActionbar
import tech.ketc.numeri.util.android.supportActBar
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import java.io.Serializable
import javax.inject.Inject

class MediaActivity : AppCompatActivity(), AutoInject, HasSupportFragmentInjector,
        IMediaUI by MediaUI() {

    @Inject lateinit var mAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: MediaViewModel by viewModel { mViewModelFactory }
    private val mInfo by lazy { intent.getSerializableExtra(EXTRA_INFO) as Info }

    private var mIsSystemUIVisible = true
    private val mSystemUiHandler = Handler()

    companion object {
        val SYSTEM_UI_ANIM_DELAY = 200L
        val SAVED_SYSTEM_UI_VISIBLE = "SAVED_SYSTEM_UI_VISIBLE"
        val EXTRA_INFO = "EXTRA_INFO"

        fun start(ctx: Context, entities: List<MediaEntity>, position: Int = 0) {
            ctx.startActivity<MediaActivity>(EXTRA_INFO to Info(entities, position))
        }
    }

    override fun supportFragmentInjector() = mAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        initializeUI()
        savedInstanceState?.run(this::restore)
        initializeUIBehavior()
        toast(mInfo.entities.joinToString("\n") { it.url })
    }

    private fun initializeUI() {
        setUpSupportActionbar(toolbar)
    }

    private fun initializeUIBehavior() {
        componentRoot.setOnClickListener { toggleSystemUIVisibility() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(SAVED_SYSTEM_UI_VISIBLE, mIsSystemUIVisible)
        super.onSaveInstanceState(outState)
    }

    private fun restore(savedState: Bundle) {
        savedState.getBoolean(SAVED_SYSTEM_UI_VISIBLE)
                .takeIf { mIsSystemUIVisible != it }
                ?.run { toggleSystemUIVisibility() }
    }

    private fun hideSystemUI() {
        supportActBar.hide()
        appBar.fadeOut()
        componentRoot.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        mIsSystemUIVisible = false
    }

    private fun executeHideSystemUI() {
        mSystemUiHandler.removeCallbacks(this::showSystemUI)
        mSystemUiHandler.postDelayed(this::hideSystemUI, SYSTEM_UI_ANIM_DELAY)
    }

    private fun showSystemUI() {
        supportActBar.show()
        appBar.fadeIn()
        componentRoot.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        mIsSystemUIVisible = true
    }

    private fun executeShowSystemUI() {
        mSystemUiHandler.removeCallbacks(this::hideSystemUI)
        mSystemUiHandler.postDelayed(this::showSystemUI, SYSTEM_UI_ANIM_DELAY)
    }

    private fun toggleSystemUIVisibility() {
        if (mIsSystemUIVisible) {
            executeHideSystemUI()
        } else {
            executeShowSystemUI()
        }
    }

    data class Info(val entities: List<MediaEntity>, val position: Int) : Serializable
}