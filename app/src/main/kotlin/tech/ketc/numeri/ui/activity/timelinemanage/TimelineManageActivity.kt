package tech.ketc.numeri.ui.activity.timelinemanage

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import org.jetbrains.anko.setContentView
import tech.ketc.numeri.R
import tech.ketc.numeri.infra.entity.TimelineGroup
import tech.ketc.numeri.ui.fragment.timelinegroup.OnGroupSelectedListener
import tech.ketc.numeri.ui.fragment.timelinegroup.TimelineGroupManageFragment
import tech.ketc.numeri.ui.fragment.timelinemanage.TimelineManageFragment
import tech.ketc.numeri.ui.model.TimelineManageViewModel
import tech.ketc.numeri.ui.view.pager.ModifiablePagerAdapter
import tech.ketc.numeri.util.android.setUpSupportActionbar
import tech.ketc.numeri.util.android.startLeftOut
import tech.ketc.numeri.util.android.supportActBar
import tech.ketc.numeri.util.android.ui.SnackbarMaker
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import javax.inject.Inject

class TimelineManageActivity : AppCompatActivity(), HasSupportFragmentInjector, AutoInject,
        ITimelineManageUI by TimelineManageUI(), OnGroupSelectedListener, SnackbarMaker,
        ViewPager.OnPageChangeListener {

    @Inject lateinit var mAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: TimelineManageViewModel by viewModel { mViewModelFactory }
    private var mOnAddFabClickListener: OnAddFabClickListener? = null
    private val mAdapter by lazy { ModifiablePagerAdapter<String, Fragment>(supportFragmentManager) }
    private val mMainContent by lazy { createMainContent() }
    private val mGroupName: String by lazy { intent.extras.getString(EXTRA_GROUP_NAME) }

    companion object {
        private val TAG_FRAGMENT_GROUP_MANAGER = "TAG_FRAGMENT_GROUP_MANAGER"
        private val EXTRA_GROUP_NAME = "EXTRA_GROUP_NAME"
        fun start(act: Activity, groupName: String = "") {
            act.startLeftOut<TimelineManageActivity>(EXTRA_GROUP_NAME to groupName)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        initializeUI()
        initializeUIBehavior()
        initialize(savedInstanceState)
    }

    private fun initializeUI() {
        setUpSupportActionbar(toolbar)
    }

    private fun initializeUIBehavior() {
        toolbar.setNavigationOnClickListener {
            if (pager.currentItem == 1) pager.currentItem = 0
            else finish()
        }
        fab.setOnClickListener {
            mOnAddFabClickListener?.onAddFabClick()
        }
    }

    private fun initialize(savedInstanceState: Bundle?) {
        pager.adapter = mAdapter
        pager.addOnPageChangeListener(this)
        if (savedInstanceState == null)
            mAdapter.setContents(arrayListOf(mMainContent))
        mGroupName.takeIf { it.isNotEmpty() }?.let {
            onGroupSelected(TimelineGroup(it))
        }
    }

    private fun createMainContent(): ModifiablePagerAdapter.Content<String, Fragment> {
        val fragment = TimelineGroupManageFragment.create().also { mOnAddFabClickListener = it }
        return ModifiablePagerAdapter.Content(TAG_FRAGMENT_GROUP_MANAGER, fragment, TAG_FRAGMENT_GROUP_MANAGER)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (pager.currentItem == 1) {
                    pager.currentItem = 0
                } else {
                    super.onKeyDown(keyCode, event)
                }
            }
            else -> return super.onKeyDown(keyCode, event)
        }
        return true
    }

    override fun finish() {
        mModel.notifyTimelineChanged()
        super.finish()
    }

    //impl interface
    override fun supportFragmentInjector(): AndroidInjector<Fragment> = mAndroidInjector

    override fun getSnackSourceView() = fab

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        mOnAddFabClickListener = mAdapter.getItem(position) as OnAddFabClickListener
        when (position) {
            0 -> supportActBar.title = getString(R.string.timeline_manage)
            1 -> supportActBar.title = mAdapter.getContent(1).name
        }
    }

    override fun onGroupSelected(group: TimelineGroup) {
        val fragment = TimelineManageFragment.create(group).also {
            mOnAddFabClickListener = it
        }
        val content = ModifiablePagerAdapter.Content(group.name, fragment, group.name)
        mAdapter.setContents(arrayListOf(mMainContent, content))
        pager.currentItem = 1
    }
}