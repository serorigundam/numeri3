package net.ketc.numeri.presentation.presenter.activity

import android.os.Bundle
import net.ketc.numeri.domain.entity.TweetsDisplayGroup
import net.ketc.numeri.domain.inject
import net.ketc.numeri.domain.service.TweetsDisplayService
import net.ketc.numeri.presentation.view.activity.CreateDisplayGroupActivity
import net.ketc.numeri.presentation.view.activity.TweetsDisplayGroupManageActivityInterface
import net.ketc.numeri.presentation.view.activity.TweetsDisplayManageActivity
import javax.inject.Inject

class TweetsDisplayGroupManagePresenter(override val activity: TweetsDisplayGroupManageActivityInterface)
    : Presenter<TweetsDisplayGroupManageActivityInterface> {

    @Inject
    lateinit var displayService: TweetsDisplayService

    init {
        inject()
    }

    override fun initialize(savedInstanceState: Bundle?) {
        displayService.getAllGroup().forEach {
            activity.add(it)
        }
    }

    fun startTweetsDisplayManageActivity(group: TweetsDisplayGroup) {
        TweetsDisplayManageActivity.start(ctx, group)
    }

    fun delete(group: TweetsDisplayGroup) {
        displayService.removeGroup(group)
        activity.remove(group)
    }

    fun startCreateDisplayGroupActivity() {
        CreateDisplayGroupActivity.start(ctx)
    }

    override fun onResume() {
        super.onResume()
        displayService.getAllGroup().singleOrNull { !activity.groups.contains(it) }?.let {
            activity.add(it)
        }
    }
}
