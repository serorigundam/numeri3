package tech.ketc.numeri.ui.fragment.timelinemanage

import android.arch.lifecycle.ViewModelProvider
import android.support.v4.app.Fragment
import tech.ketc.numeri.ui.activity.timelinemanage.OnAddFabClickListener
import tech.ketc.numeri.ui.model.TimelineManageViewModel
import tech.ketc.numeri.util.arch.viewmodel.commonViewModel
import javax.inject.Inject

class TimelineManageFragment : Fragment(), OnAddFabClickListener {
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: TimelineManageViewModel by commonViewModel { mViewModelFactory }

    override fun onAddFabClick() {
    }
}