package net.ketc.numeri.presentation.view.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import net.ketc.numeri.domain.entity.TweetsDisplayGroup
import net.ketc.numeri.presentation.presenter.activity.TweetsDisplayGroupManagePresenter
import net.ketc.numeri.presentation.view.activity.ui.TweetsDisplayGroupManageActivityUI
import net.ketc.numeri.presentation.view.component.TweetsDisplayGroupsRecyclerAdapter
import net.ketc.numeri.util.android.SimpleItemTouchHelper
import net.ketc.numeri.util.android.defaultInit
import org.jetbrains.anko.setContentView

class TweetsDisplayGroupManageActivity :
        ApplicationActivity<TweetsDisplayGroupManagePresenter>(), TweetsDisplayGroupManageActivityInterface {
    override val ctx: Context
        get() = this
    override val presenter: TweetsDisplayGroupManagePresenter = TweetsDisplayGroupManagePresenter(this)
    private val ui = TweetsDisplayGroupManageActivityUI()
    private val groupsRecycler: RecyclerView by lazy { ui.groupsRecycler }
    private val toolbar: Toolbar by lazy { ui.toolbar }
    private val adapter = TweetsDisplayGroupsRecyclerAdapter { presenter.startTweetsDisplayManageActivity(it) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui.setContentView(this)
        setSupportActionBar(toolbar)
        groupsRecycler.defaultInit()
        groupsRecycler.adapter = adapter
        SimpleItemTouchHelper(swipeEnable = true, onSwiped = { vh, d ->
            presenter.delete(adapter.get(vh.adapterPosition))
        }).attachToRecyclerView(groupsRecycler)
        ui.addButton.setOnClickListener { presenter.addGroup() }
        presenter.initialize(savedInstanceState)
    }

    override fun add(group: TweetsDisplayGroup) = adapter.add(group)

    override fun remove(group: TweetsDisplayGroup) = adapter.remove(group)

}

interface TweetsDisplayGroupManageActivityInterface : ActivityInterface {
    fun add(group: TweetsDisplayGroup)
    fun remove(group: TweetsDisplayGroup)
}