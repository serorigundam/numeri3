package net.ketc.app

import android.content.Context
import android.content.res.Configuration
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.drawerLayout

class MainActivity : AppCompatActivity() {
    private val drawerToggle: ActionBarDrawerToggle by lazy { ActionBarDrawerToggle(this, drawer, 0, 0) }
    private val drawer: DrawerLayout by lazy { find<DrawerLayout>(R.id.drawer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivityUI().setContentView(this)
        setSupportActionBar(find<Toolbar>(R.id.toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        drawer.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
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

}


class MainActivityUI : AnkoComponent<MainActivity> {
    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        drawerLayout {
            id = R.id.drawer
            coordinatorLayout {
                appBarLayout {
                    toolbar {
                        id = R.id.toolbar
                    }.lparams {
                        height = wrapContent
                        width = matchParent
                        scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or
                                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    }
                }.lparams {
                    height = wrapContent
                    width = matchParent
                }
                recyclerView {
                    adapter = Adapter()
                    layoutManager = LinearLayoutManager(ui.ctx, LinearLayoutManager.VERTICAL, false)
                    itemAnimator = DefaultItemAnimator()
                }.lparams {
                    height = matchParent
                    width = matchParent
                    behavior = AppBarLayout.ScrollingViewBehavior()
                }
            }
            navigationView {
                inflateMenu(R.menu.main_navigation)
            }.lparams(wrapContent, matchParent) {
                gravity = Gravity.START
            }
        }
    }

    class Adapter() : RecyclerView.Adapter<Adapter.VH>() {
        override fun onBindViewHolder(holder: VH?, position: Int) {
        }

        override fun getItemCount() = 3

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(parent.context)

        class VH(context: Context) : RecyclerView.ViewHolder(TextView(context).apply { text = "hoge" })
    }
}