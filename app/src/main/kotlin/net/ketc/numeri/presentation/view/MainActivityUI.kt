package net.ketc.numeri.presentation.view

import android.content.Context
import android.graphics.Color
import android.support.design.widget.AppBarLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import net.ketc.numeri.R
import net.ketc.numeri.util.android.getResourceId
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.drawerLayout

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
                id = R.id.navigation
                lparams(wrapContent, matchParent) {
                    gravity = Gravity.START
                }
                inflateMenu(R.menu.main_navigation)
                addHeaderView(navigationHeader(ctx))
                navigationContent()
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
                    leftOf(R.id.show_account_indicator)
                    alignParentStart()
                    centerVertically()
                    margin = dimen(R.dimen.margin_text_medium)
                }

                imageView {
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
        visibility = View.GONE
        topPadding = dip(168)
        lparams(matchParent, matchParent)

        relativeLayout {
            lparams(matchParent, matchParent)
            linearLayout {
                id = R.id.accounts_linear
                lparams(matchParent, wrapContent) {
                    backgroundColor = context.getColor(R.color.transparent)
                }
                orientation = LinearLayout.VERTICAL
            }
            relativeLayout {
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

    class Adapter() : RecyclerView.Adapter<Adapter.VH>() {
        override fun onBindViewHolder(holder: VH?, position: Int) {
        }

        override fun getItemCount() = 3

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(parent.context)

        class VH(context: Context) : RecyclerView.ViewHolder(TextView(context).apply { text = "hoge" })
    }
}