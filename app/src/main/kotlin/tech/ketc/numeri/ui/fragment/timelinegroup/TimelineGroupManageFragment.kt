package tech.ketc.numeri.ui.fragment.timelinegroup

import android.arch.lifecycle.ViewModelProvider
import android.support.v4.app.Fragment
import tech.ketc.numeri.ui.model.TimelineManageViewModel
import tech.ketc.numeri.util.arch.viewmodel.commonViewModel
import javax.inject.Inject

class TimelineGroupManageFragment : Fragment() {
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: TimelineManageViewModel by commonViewModel { mViewModelFactory }
}
