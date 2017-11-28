package tech.ketc.numeri.ui.activity.media

import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.github.chrisbanes.photoview.PhotoView
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.twitter.model.MediaEntity
import tech.ketc.numeri.ui.fragment.dialog.MessageDialogFragment
import tech.ketc.numeri.ui.fragment.dialog.OnDialogItemSelectedListener
import tech.ketc.numeri.ui.model.MediaViewModel
import tech.ketc.numeri.ui.view.media.photoView
import tech.ketc.numeri.ui.view.pager.SimplePagerAdapter
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.android.*
import tech.ketc.numeri.util.anko.UIComponent
import tech.ketc.numeri.util.arch.coroutine.asyncResponse
import tech.ketc.numeri.util.arch.owner.bindLaunch
import tech.ketc.numeri.util.arch.response.orError
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import tech.ketc.numeri.util.logTag
import java.io.Serializable
import javax.inject.Inject

class MediaActivity : AppCompatActivity(), AutoInject, IMediaUI by MediaUI() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: MediaViewModel by viewModel { mViewModelFactory }
    private val mInfo by lazy { intent.getSerializableExtra(EXTRA_INFO) as Info }

    private var mIsSystemUIVisible = true
    private val mSystemUiHandler = Handler()
    private var mCurrentPosition = 0
    private val mAdapter by lazy { SimplePagerAdapter(supportFragmentManager) }
    private var mIsSaveForEachUser = false

    companion object {
        private val SYSTEM_UI_ANIM_DELAY = 200L
        private val EXTRA_INFO = "EXTRA_INFO"
        private val SAVED_SYSTEM_UI_VISIBLE = "SAVED_SYSTEM_UI_VISIBLE"
        private val SAVED_POSITION = "SAVED_POSITION"
        private val PREF_SAVE_FOR_EACH_USER = "tech:ketc:numer:media:PREF_SAVE_FOR_EACH_USER"
        fun start(ctx: Context, entities: List<MediaEntity>, screenName: String, position: Int = 0) {
            ctx.startActivity<MediaActivity>(EXTRA_INFO to Info(entities, screenName, position))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        initializeUI()
        savedInstanceState?.run(this::restore)
        initializeUIBehavior()
        initialize(savedInstanceState)
    }

    private fun initializeUI() {
        setUpSupportActionbar(toolbar)
        toolbar.setFinishWithNavigationClick(this)
        supportActBar.title = mInfo.screenName
    }

    private fun initializeUIBehavior() {
        componentRoot.setOnClickListener { toggleSystemUIVisibility() }
        pager.setOnClickListener { toggleSystemUIVisibility() }
    }

    private fun initialize(savedInstanceState: Bundle?) {
        mCurrentPosition = if (savedInstanceState == null) {
            val contents = mInfo.entities.map { MediaFragment.create(mInfo.screenName, it) }
            mAdapter.setContents(*contents.toTypedArray())
            mInfo.position
        } else {
            savedInstanceState.getInt(SAVED_POSITION)
        }
        mIsSaveForEachUser = pref.getBoolean(PREF_SAVE_FOR_EACH_USER, false)
        pager.adapter = mAdapter
        pager.currentItem = mCurrentPosition
        setSubtitle(mCurrentPosition)
        pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                setSubtitle(position)
                mCurrentPosition = position
            }
        })
    }

    private fun setSubtitle(position: Int) {
        supportActBar.subtitle = "${position + 1} / ${mInfo.entities.size}"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(SAVED_SYSTEM_UI_VISIBLE, mIsSystemUIVisible)
        outState.putInt(SAVED_POSITION, mCurrentPosition)
        super.onSaveInstanceState(outState)
    }

    private fun restore(savedState: Bundle) {
        savedState.getBoolean(SAVED_SYSTEM_UI_VISIBLE)
                .takeIf { mIsSystemUIVisible != it }
                ?.run { toggleSystemUIVisibility() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        MenuInflater(this).inflate(R.menu.menu_media, menu)
        menu.findItem(R.id.is_save_for_each_user).isChecked = mIsSaveForEachUser
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.is_save_for_each_user -> {
                mIsSaveForEachUser = !mIsSaveForEachUser
                item.isChecked = mIsSaveForEachUser
                pref.edit().putBoolean(PREF_SAVE_FOR_EACH_USER, mIsSaveForEachUser)
                        .apply()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
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

    data class Info(val entities: List<MediaEntity>, val screenName: String, val position: Int) : Serializable

    private interface IMediaUIComponent : UIComponent<RelativeLayout> {
        val photoView: PhotoView
        val progressBar: ProgressBar
    }

    private class MediaUIComponent : IMediaUIComponent {
        override lateinit var componentRoot: RelativeLayout
            private set
        override lateinit var photoView: PhotoView
            private set
        override lateinit var progressBar: ProgressBar
            private set

        override fun createView(ctx: Context) = ctx.relativeLayout {
            componentRoot = this
            photoView {
                photoView = this
                setZoomable(true)
            }.lparams(matchParent, matchParent)

            progressBar {
                progressBar = this
                visibility = View.VISIBLE
            }.lparams(dip(64), dip(64)) {
                centerInParent()
                scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
            }
        }
    }

    class MediaFragment : Fragment(), IMediaUIComponent by MediaUIComponent(), OnDialogItemSelectedListener {
        private val mInfo by lazy { arg.getSerializable(EXTRA_INFO) as Info }
        private val mParentActivity by lazy { (act as MediaActivity) }


        private val mModel: MediaViewModel
            get() = mParentActivity.mModel

        private val mUrl
            get() = mInfo.entity.url + ":orig"

        companion object {
            private val EXTRA_INFO = "EXTRA_INFO"
            private val REQUEST_SAVE = 100
            private val TAG_SAVE = "TAG_SAVE"
            private val SAVED_INITIALIZED = "SAVED_INITIALIZED"
            fun create(screenName: String, entity: MediaEntity) = MediaFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_INFO, Info(screenName, entity))
                }
            }
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
                = createView(ctx)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            if (!mModel.isError(mUrl).also { Logger.v(logTag, "$mUrl isError $it") }) {
                mModel.observeBitmapContent(this, mUrl) { (bitmap, _) ->
                    Logger.v(logTag, "observe on $bitmap")
                    progressBar.visibility = View.GONE
                    photoView.setImageBitmap(bitmap)
                    photoView.setOnClickListener { mParentActivity.toggleSystemUIVisibility() }
                    photoView.setOnLongClickListener {
                        MessageDialogFragment.create(REQUEST_SAVE, getString(R.string.message_image_save),
                                positiveId = R.string.save).show(childFragmentManager, TAG_SAVE)
                        true
                    }
                    photoView.setOnSingleFlingListener { _, _, _, velocityY ->
                        if (velocityY !in -5000..5000) {
                            mParentActivity.executeHideSystemUI()
                            mParentActivity.finish()
                            if (velocityY > 0) {
                                mParentActivity.overridePendingTransition(R.anim.fade_in, R.anim.bottom_out)
                            } else {
                                mParentActivity.overridePendingTransition(R.anim.fade_in, R.anim.top_out)
                            }
                        }
                        true
                    }
                }
            } else {
                progressBar.visibility = View.GONE
            }
            if (savedInstanceState == null)
                loadImage()
        }

        override fun onSaveInstanceState(outState: Bundle) {
            //for initial create judgment
            outState.putString(SAVED_INITIALIZED, "initialized")
            super.onSaveInstanceState(outState)
        }

        private fun loadImage() {
            val url = mUrl
            bindLaunch {
                val content = mModel.loadImage(url, false).await().orError {
                    mModel.putError(url)
                    toast("${getString(R.string.failed_get_image)} : $url")
                } ?: return@bindLaunch
                mModel.putBitmapContent(url, content)
            }
        }

        private fun save() {
            mModel.observeBitmapContent(this, mUrl) { (bitmap, mimeType) ->
                bindLaunch {
                    var directory = "numetter"
                    if (mParentActivity.mIsSaveForEachUser) {
                        directory += "/${mInfo.screenName}"
                    }
                    asyncResponse {
                        bitmap.save(ctx, mimeType, directory, quality = 100)
                    }.await().orError {
                        toast(R.string.save_failure)
                    } ?: return@bindLaunch
                    toast(R.string.save_complete)
                }
            }
        }

        override fun onDialogItemSelected(requestCode: Int, itemId: Int) {
            if (requestCode != REQUEST_SAVE || itemId != R.string.save) return
            save()
        }

        data class Info(val screenName: String, val entity: MediaEntity) : Serializable
    }
}