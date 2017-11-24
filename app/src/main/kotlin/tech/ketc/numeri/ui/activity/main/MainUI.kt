package tech.ketc.numeri.ui.activity.main

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
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.support.v4.drawerLayout
import tech.ketc.numeri.R
import tech.ketc.numeri.util.android.getResourceId
import tech.ketc.numeri.util.anko.component
import tech.ketc.numeri.util.anko.create
import tech.ketc.numeri.util.anko.startOf

class MainUI : IMainUI {
    override lateinit var drawer: DrawerLayout
        private set
    override lateinit var navigation: NavigationView
        private set
    override lateinit var columnGroupWrapper: CoordinatorLayout
        private set
    override lateinit var toolbar: Toolbar
        private set
    override lateinit var tweetFab: FloatingActionButton
        private set
    override lateinit var accountListUI: IMainUI.IAccountListUI
        private set
    override lateinit var navigationHeaderUI: IMainUI.INavigationHeaderUI
        private set
    override lateinit var groupChangeFab: FloatingActionButton
        private set

    override fun createView(ui: AnkoContext<MainActivity>) = ui.create {
        drawerLayout {
            drawer = this
            coordinatorLayout {
                appBarLayout {
                    toolbar {
                        toolbar = this
                    }.lparams(matchParent, wrapContent) {
                        scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or
                                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    }
                }.lparams(matchParent, wrapContent)
                coordinatorLayout {
                    columnGroupWrapper = this
                    id = R.id.column_group_wrapper
                }.lparams(matchParent, matchParent) {
                    behavior = AppBarLayout.ScrollingViewBehavior()
                }

                floatingActionButton {
                    groupChangeFab = this
                    image = ctx.getDrawable(R.drawable.ic_view_carousel_white_24dp)
                    size = FloatingActionButton.SIZE_MINI
                }.lparams(dip(40), dip(40)) {
                    anchorGravity = Gravity.BOTTOM or Gravity.END
                    margin = dip(25)
                    anchorId = R.id.column_group_wrapper
                }

                floatingActionButton {
                    tweetFab = this
                    image = ctx.getDrawable(R.drawable.ic_mode_edit_white_24dp)
                    size = FloatingActionButton.SIZE_AUTO
                }.lparams {
                    margin = dimen(R.dimen.margin_medium)
                    anchorGravity = Gravity.BOTTOM or Gravity.END
                    anchorId = R.id.column_group_wrapper
                }
            }

            navigationView {
                navigation = this
                inflateMenu(R.menu.main_navigation)

                addHeaderView(NavigationHeaderUIComponent().also {
                    navigationHeaderUI = it
                }.createView(ctx))

                component(AccountListUIComponent().also {
                    accountListUI = it
                })
            }.lparams(wrapContent, matchParent) {
                gravity = Gravity.START
            }
        }
    }


    class NavigationHeaderUIComponent : IMainUI.INavigationHeaderUI {
        override lateinit var componentRoot: RelativeLayout
            private set
        override lateinit var navigationStateIndicator: ImageView
            private set
        override lateinit var toggleNavigationStateButton: RelativeLayout
            private set


        override fun createView(ctx: Context) = ctx.relativeLayout {
            componentRoot = this
            lparams(matchParent, dip(160))
            backgroundColor = Color.parseColor("#10505050")
            imageView {
                image = ctx.getDrawable(R.drawable.ic_launcher)
            }.lparams(dimen(R.dimen.image_icon_large), dimen(R.dimen.image_icon_large)) {
                topMargin = dimen(R.dimen.margin_medium)
                marginStart = dimen(R.dimen.margin_medium)
            }

            relativeLayout {
                toggleNavigationStateButton = this
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
                        text = "アカウント一覧"
                        lines = 1
                    }.lparams {
                        startOf(R.id.show_account_indicator)
                        alignParentStart()
                        centerVertically()
                        margin = dimen(R.dimen.margin_text_medium)
                    }

                    imageView {
                        navigationStateIndicator = this
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
    }

    class AccountListUIComponent : IMainUI.IAccountListUI {
        override lateinit var componentRoot: RelativeLayout
            private set
        override lateinit var accountList: ViewGroup
            private set
        override lateinit var addAccountButton: View
            private set

        override fun createView(ctx: Context) = ctx.relativeLayout {
            lparams(matchParent, matchParent)
            componentRoot = this
            visibility = View.GONE
            topPadding = dip(168)
            relativeLayout {
                lparams(matchParent, matchParent)
                linearLayout {
                    accountList = this
                    id = R.id.accounts_linear
                    lparams(matchParent, wrapContent) {
                        backgroundColor = context.getColor(R.color.transparent)
                    }
                    orientation = LinearLayout.VERTICAL
                }
                relativeLayout {
                    addAccountButton = this
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
}