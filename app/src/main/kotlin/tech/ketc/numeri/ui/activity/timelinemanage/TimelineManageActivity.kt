package tech.ketc.numeri.ui.activity.timelinemanage

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.toast
import tech.ketc.numeri.infra.entity.TimelineGroup
import tech.ketc.numeri.ui.fragment.timelinegroup.OnGroupSelectedListener
import tech.ketc.numeri.ui.fragment.timelinegroup.TimelineGroupManageFragment
import tech.ketc.numeri.ui.model.TimelineManageViewModel
import tech.ketc.numeri.util.android.setFinishWithNavigationClick
import tech.ketc.numeri.util.android.setUpSupportActionbar
import tech.ketc.numeri.util.android.ui.SnackbarMaker
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import javax.inject.Inject

class TimelineManageActivity : AppCompatActivity(), HasSupportFragmentInjector, AutoInject,
        ITimelineManageUI by TimelineManageUI(), OnGroupSelectedListener, SnackbarMaker {
    @Inject lateinit var mAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: TimelineManageViewModel by viewModel { mViewModelFactory }
    private var mOnAddFabClickListener: OnAddFabClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        initializeUI()
        initializeUIBehavior()
        if (savedInstanceState == null) initialize()
    }

    private fun initializeUI() {
        setUpSupportActionbar(toolbar)
    }

    private fun initializeUIBehavior() {
        toolbar.setFinishWithNavigationClick(this)
        fab.setOnClickListener {
            mOnAddFabClickListener?.onAddFabClick()
        }
    }

    private fun initialize() {
        val fragment = TimelineGroupManageFragment.create()
                .also { mOnAddFabClickListener = it }
        supportFragmentManager.beginTransaction()
                .replace(fragmentView.id, fragment)
                .commit()
    }


    //impl interface
    override fun supportFragmentInjector(): AndroidInjector<Fragment> = mAndroidInjector

    override fun getSnackSourceView() = fab

    override fun onGroupSelected(group: TimelineGroup) {
        toast("onGroupSelected :${group.name}")
    }
}