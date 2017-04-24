package net.ketc.numeri.presentation.view.activity

import android.content.Context
import android.os.Bundle
import net.ketc.numeri.domain.entity.TweetsDisplayGroup
import net.ketc.numeri.presentation.presenter.activity.TweetsDisplayGroupManagePresenter
import net.ketc.numeri.presentation.view.activity.ui.ITweetsDisplayGroupManageActivityUI
import net.ketc.numeri.presentation.view.activity.ui.TweetsDisplayGroupManageActivityUI
import net.ketc.numeri.presentation.view.component.TweetsDisplayGroupsRecyclerAdapter
import net.ketc.numeri.util.android.SimpleItemTouchHelper
import net.ketc.numeri.util.android.defaultInit
import net.ketc.numeri.util.copy
import org.jetbrains.anko.setContentView

class TweetsDisplayGroupManageActivity :
        ApplicationActivity<TweetsDisplayGroupManagePresenter>(),
        TweetsDisplayGroupManageActivityInterface,
        ITweetsDisplayGroupManageActivityUI by TweetsDisplayGroupManageActivityUI() {

    override val groups: List<TweetsDisplayGroup>
        get() = mGroups.copy()

    private var mGroups = ArrayList<TweetsDisplayGroup>()

    override val ctx: Context
        get() = this
    override val presenter: TweetsDisplayGroupManagePresenter = TweetsDisplayGroupManagePresenter(this)
    private val adapter = TweetsDisplayGroupsRecyclerAdapter { presenter.startTweetsDisplayManageActivity(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        groupsRecycler.defaultInit()
        groupsRecycler.adapter = adapter
        SimpleItemTouchHelper(swipeEnable = true, onSwiped = { vh, _ ->
            presenter.delete(adapter.get(vh.adapterPosition))
        }).attachToRecyclerView(groupsRecycler)
        addButton.setOnClickListener { presenter.startCreateDisplayGroupActivity() }
        presenter.initialize(savedInstanceState)
    }

    override fun add(group: TweetsDisplayGroup) {
        adapter.add(group)
        mGroups.add(group)
    }

    override fun remove(group: TweetsDisplayGroup) {
        adapter.remove(group)
        mGroups.remove(group)
    }
}

interface TweetsDisplayGroupManageActivityInterface : ActivityInterface {
    val groups: List<TweetsDisplayGroup>
    fun add(group: TweetsDisplayGroup)
    fun remove(group: TweetsDisplayGroup)
}