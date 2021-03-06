package net.ketc.numeri.presentation.view.activity.ui

import android.content.Context
import android.graphics.Color
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import net.ketc.numeri.R
import net.ketc.numeri.presentation.view.activity.MainActivity
import net.ketc.numeri.util.android.getResourceId
import net.ketc.numeri.util.android.startOf
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.support.v4.drawerLayout

class MainActivityUI : IMainActivityUI {
    override lateinit var drawer: DrawerLayout
        private set
    override lateinit var navigation: NavigationView
        private set
    override lateinit var showAccountIndicator: ImageView
        private set
    override lateinit var navigationContent: RelativeLayout
        private set
    override lateinit var showAccountRelative: RelativeLayout
        private set
    override lateinit var addAccountButton: RelativeLayout
        private set
    override lateinit var accountsLinear: LinearLayout
        private set
    override lateinit var columnGroupWrapper: CoordinatorLayout
        private set
    override lateinit var toolbar: Toolbar
        private set
    override lateinit var tweetButton: FloatingActionButton
        private set

    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        drawerLayout {
            drawer = this
            id = R.id.drawer
            coordinatorLayout {
                appBarLayout {
                    toolbar {
                        id = R.id.toolbar
                        toolbar = this
                    }.lparams(matchParent, wrapContent) {
                        scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or
                                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    }
                }.lparams(matchParent, wrapContent)
                coordinatorLayout {
                    columnGroupWrapper = this
                    id = R.id.column_group_wrapper_coordinator
                }.lparams(matchParent, matchParent) {
                    behavior = AppBarLayout.ScrollingViewBehavior()
                }

                floatingActionButton {
                    tweetButton = this
                    image = ctx.getDrawable(R.drawable.ic_mode_edit_white_24dp)
                    size = FloatingActionButton.SIZE_AUTO
                }.lparams {
                    margin = dimen(R.dimen.margin_medium)
                    anchorGravity = Gravity.BOTTOM or Gravity.END
                    anchorId = R.id.column_group_wrapper_coordinator
                }
            }

            navigationView {
                id = R.id.navigation
                navigation = this
                inflateMenu(R.menu.main_navigation)
                addHeaderView(navigationHeader(ctx))
                navigationContent()
            }.lparams(wrapContent, matchParent) {
                gravity = Gravity.START
            }
        }
    }

    private fun navigationHeader(ctx: Context) = ctx.relativeLayout {
        lparams(matchParent, dip(160))
        backgroundColor = Color.parseColor("#10505050")
        imageView {
            id = R.id.icon_image
            image = ctx.getDrawable(R.mipmap.ic_launcher)
        }.lparams(dimen(R.dimen.image_icon_large), dimen(R.dimen.image_icon_large)) {
            topMargin = dimen(R.dimen.margin_medium)
            marginStart = dimen(R.dimen.margin_medium)
        }

        relativeLayout {
            showAccountRelative = this
            id = R.id.show_account_relative
            lparams(matchParent, dip(72)) {
                alignParentBottom()
            }

            relativeLayout {
                lparams {
                    alignParentBottom()
                    bottomMargin = dimen(R.dimen.margin_small)
                }
                backgroundResource = ctx.getResourceId(android.R.attr.selectableItemBackground)
                textView {
                    id = R.id.user_name_text
                    text = "アカウント一覧"
                    lines = 1
                }.lparams {
                    startOf(R.id.show_account_indicator)
                    alignParentStart()
                    centerVertically()
                    margin = dimen(R.dimen.margin_text_medium)
                }

                imageView {
                    showAccountIndicator = this
                    id = R.id.show_account_indicator
                    image = ctx.getDrawable(R.drawable.ic_expand_more_white_24dp)
                    backgroundColor = ctx.getColor(R.color.transparent)
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                }.lparams(dip(16), dip(16)) {
                    alignParentEnd()
                    centerVertically()
                    marginEnd = dimen(R.dimen.margin_text_medium)
                }
            }
        }
    }

    private fun ViewManager.navigationContent() = relativeLayout {
        id = R.id.navigation_content
        navigationContent = this
        visibility = View.GONE
        topPadding = dip(168)
        lparams(matchParent, matchParent)

        relativeLayout {
            lparams(matchParent, matchParent)
            linearLayout {
                accountsLinear = this
                id = R.id.accounts_linear
                lparams(matchParent, wrapContent) {
                    backgroundColor = context.getColor(R.color.transparent)
                }
                orientation = LinearLayout.VERTICAL
            }
            relativeLayout {
                addAccountButton = this
                id = R.id.add_account_button
                lparams(matchParent, dip(48)) {
                    below(R.id.accounts_linear)
                }
                backgroundResource = context.getResourceId(android.R.attr.selectableItemBackground)
                isClickable = true
                textView {
                    text = context.getString(R.string.add_account)
                    lines = 1
                }.lparams(matchParent, wrapContent) {
                    centerVertically()
                    marginStart = dimen(R.dimen.margin_medium)
                    marginEnd = dimen(R.dimen.margin_medium)
                }
            }
        }
    }
}

interface IMainActivityUI : AnkoComponent<MainActivity> {
    val toolbar: Toolbar
    val drawer: DrawerLayout
    val navigation: NavigationView
    val showAccountIndicator: ImageView
    val navigationContent: RelativeLayout
    val showAccountRelative: RelativeLayout
    val addAccountButton: RelativeLayout
    val accountsLinear: LinearLayout
    val columnGroupWrapper: CoordinatorLayout
    val tweetButton: FloatingActionButton
}