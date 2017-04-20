package net.ketc.numeri.presentation.view.activity

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import net.ketc.numeri.R
import net.ketc.numeri.domain.entity.TweetsDisplay
import net.ketc.numeri.domain.entity.TweetsDisplayGroup
import net.ketc.numeri.domain.model.TwitterUser
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.presenter.activity.TweetsDisplayManagePresenter
import net.ketc.numeri.presentation.view.activity.ui.TweetsDisplayManageActivityUI
import net.ketc.numeri.presentation.view.component.TweetsDisplayRecyclerAdapter
import net.ketc.numeri.presentation.view.component.ui.menu.createIconMenu
import net.ketc.numeri.util.android.SimpleItemTouchHelper
import net.ketc.numeri.util.android.defaultInit
import org.jetbrains.anko.find
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.startActivity

class TweetsDisplayManageActivity : ApplicationActivity<TweetsDisplayManagePresenter>(), TweetsDisplayManageActivityInterface {

    override val presenter: TweetsDisplayManagePresenter = TweetsDisplayManagePresenter(this)
    private val drawer: DrawerLayout by lazy { find<DrawerLayout>(R.id.drawer) }
    private val drawerToggle: ActionBarDrawerToggle by lazy { ActionBarDrawerToggle(this, drawer, 0, 0) }
    override val group: TweetsDisplayGroup
        get() = intent.getSerializableExtra(EXTRA_GROUP) as TweetsDisplayGroup
    override val ctx: Context
        get() = this
    private val ui = TweetsDisplayManageActivityUI()
    private val adapter: TweetsDisplayRecyclerAdapter by lazy { TweetsDisplayRecyclerAdapter() }
    private val toolbar: Toolbar by lazy { find<Toolbar>(R.id.toolbar) }
    private val displaysRecycler: RecyclerView by lazy { find<RecyclerView>(R.id.displays_recycler) }
    private val navigation: RelativeLayout by lazy { find<RelativeLayout>(R.id.navigation) }
    private val navigationContent by lazy { find<LinearLayout>(R.id.navigation_content) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui.setContentView(this)
        setSupportActionBar(toolbar)
        drawer.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.subtitle = group.name
        displaysRecycler.adapter = adapter
        displaysRecycler.defaultInit()
        presenter.initialize(savedInstanceState)
        SimpleItemTouchHelper(moveEnable = true, swipeEnable = true,
                onMove = { _, vh, target ->
                    val by = vh.adapterPosition
                    val to = target.adapterPosition
                    val lastIndex = adapter.displayItemCount
                    if (by <= lastIndex && to <= lastIndex) {
                        presenter.replace(adapter.get(to), adapter.get(by))
                    }
                    true
                },
                onSwiped = { vh, _ ->
                    val position = vh.adapterPosition
                    if (adapter.displayItemCount > position)
                        presenter.removeDisplay(adapter.get(position))
                }).attachToRecyclerView(displaysRecycler)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (drawer.isDrawerOpen(navigation)) {
                    drawer.closeDrawer(navigation)
                } else {
                    return super.onKeyDown(keyCode, event)
                }
            }
            else -> return super.onKeyDown(keyCode, event)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    private fun createClientView(clientUser: TwitterUser): View {
        val view = createIconMenu(ctx, R.drawable.ic_account_circle_white_24dp, clientUser.screenName)
        view.isClickable = false
        return view
    }

    private fun createDisplayView(display: TweetsDisplay, client: TwitterClient, text: String) = createIconMenu(ctx, R.drawable.ic_add_white_24dp, text) {
        presenter.addDisplay(display, text, client)
    }

    override fun addDisplays(clientPair: Pair<TwitterClient, TwitterUser>, displays: List<Pair<TweetsDisplay, String>>) {
        navigationContent.addView(createClientView(clientPair.second))
        displays.forEach {
            navigationContent.addView(createDisplayView(it.first, clientPair.first, it.second))
        }
    }

    override fun replace(to: TweetsDisplay, by: TweetsDisplay) = adapter.replace(to, by)

    override fun add(display: Pair<TweetsDisplay, String>) = adapter.add(display)

    override fun remove(display: TweetsDisplay) = adapter.remove(display)

    override fun closeNavigation() {
        if (drawer.isDrawerOpen(navigation)) {
            drawer.closeDrawer(navigation)
        }
    }

    companion object {
        val EXTRA_GROUP = "EXTRA_GROUP"
        fun start(ctx: Context, group: TweetsDisplayGroup) {
            ctx.startActivity<TweetsDisplayManageActivity>(EXTRA_GROUP to group)
        }
    }
}

interface TweetsDisplayManageActivityInterface : ActivityInterface {
    val group: TweetsDisplayGroup
    fun addDisplays(clientPair: Pair<TwitterClient, TwitterUser>, displays: List<Pair<TweetsDisplay, String>>)
    fun replace(to: TweetsDisplay, by: TweetsDisplay)
    fun add(display: Pair<TweetsDisplay, String>)
    fun remove(display: TweetsDisplay)

    fun closeNavigation()

}