package net.ketc.numeri.presentation.view.activity.ui

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.v7.widget.Toolbar
import net.ketc.numeri.presentation.view.activity.UserInfoActivity
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.collapsingToolbarLayout
import org.jetbrains.anko.design.coordinatorLayout
import android.support.design.widget.AppBarLayout.LayoutParams.*
import android.support.design.widget.CollapsingToolbarLayout
import org.jetbrains.anko.*
import android.support.design.widget.CollapsingToolbarLayout.LayoutParams.*
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v4.widget.SwipeRefreshLayout
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.*
import net.ketc.numeri.R
import net.ketc.numeri.util.android.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.design.tabLayout
import org.jetbrains.anko.support.v4.swipeRefreshLayout
import org.jetbrains.anko.support.v4.viewPager

class UserInfoActivityUI : IUserInfoActivityUI {
    override lateinit var swipeRefresh: SwipeRefreshLayout
        private set

    override lateinit var appBar: AppBarLayout
        private set

    override lateinit var toolbar: Toolbar
        private set

    override lateinit var collapsingToolbar: CollapsingToolbarLayout
        private set

    override lateinit var headerImage: ImageView
        private set

    override lateinit var iconRelative: RelativeLayout
        private set

    override lateinit var iconImage: ImageView
        private set

    override lateinit var infoRelative: RelativeLayout
        private set

    override lateinit var userNameText: TextView
        private set

    override lateinit var screenNameText: TextView
        private set

    override lateinit var descriptionText: TextView
        private set

    override lateinit var locationText: TextView
        private set

    override lateinit var subInfoText: TextView
        private set

    override lateinit var userProfileTabLayout: TabLayout
        private set

    override lateinit var pager: ViewPager
        private set

    override lateinit var followButton: ImageButton
        private set

    override lateinit var protectedImage: ImageView
        private set

    override lateinit var relationInfoText: TextView
        private set

    override lateinit var profileEditButton: Button
        private set

    override fun createView(ui: AnkoContext<UserInfoActivity>) = with(ui) {
        swipeRefreshLayout {
            swipeRefresh = this
            isEnabled = false
            coordinatorLayout {
                appBarLayout {
                    appBar = this
                    backgroundColor = color(R.color.colorPrimaryDark)
                    collapsingToolbarLayout {
                        collapsingToolbar = this
                        isTitleEnabled = false
                        setContentScrimColor(color(R.color.transparent))
                        userProfileContent(this)
                        userProfileHeader(this)
                        userProfileIcon(this)
                        toolbar {
                            toolbar = this
                            background = drawable(R.drawable.app_bar_gradation)
                            navigationIcon = drawable(resourceId(android.R.attr.homeAsUpIndicator))
                        }.collapsingToolbarlparams(matchParent, dimen(R.dimen.app_bar_standard_height)) {
                            expandedTitleGravity = Gravity.BOTTOM or Gravity.START
                            collapseMode = COLLAPSE_MODE_PIN
                        }
                    }.lparams(matchParent, matchParent) {
                        scrollFlags = SCROLL_FLAG_SCROLL or SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
                    }
                }.lparams(matchParent, wrapContent)

                coordinatorLayout {
                    relativeLayout {
                        tabLayout {
                            userProfileTabLayout = this
                            id = R.id.user_profile_tab
                            tabMode = TabLayout.MODE_SCROLLABLE
                        }.lparams(matchParent, wrapContent)
                        viewPager {
                            pager = this
                            id = R.id.pager
                            offscreenPageLimit = 5
                        }.lparams(matchParent, matchParent) {
                            below(R.id.user_profile_tab)
                        }
                    }.lparams(matchParent, matchParent)
                }.lparams(matchParent, matchParent) {
                    behavior = AppBarLayout.ScrollingViewBehavior()
                }
            }
        }
    }

    //collapsingToolbarContent
    private fun AnkoContext<*>.userProfileContent(manager: ViewManager) = manager.relativeLayout {
        infoRelative = this
        headerImageView {
            id = R.id.dummy_header
            visibility = View.INVISIBLE
        }.lparams(matchParent, wrapContent)

        relativeLayout {
            id = R.id.buttons_relative
            imageButton {
                followButton = this
                isClickable = false
                background = drawable(R.drawable.ripple_frame)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                visibility = View.INVISIBLE
            }.lparams(matchParent, matchParent)
            button {
                profileEditButton = this
                visibility = View.GONE
                text = string(R.string.edit_profile)
            }.lparams(matchParent, matchParent)
        }.lparams(dip(72), dip(40)) {
            marginTop = dimen(R.dimen.margin_medium)
            marginEnd = dimen(R.dimen.margin_medium)
            below(R.id.dummy_header)
            alignParentEnd()
        }

        relativeLayout {
            textView {
                userNameText = this
                id = R.id.user_name_text
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                textSizeDimen = R.dimen.text_size_large
                textColor = color(resourceId(android.R.attr.textColorPrimary))
            }.lparams(wrapContent, wrapContent) {
                marginBottom = dimen(R.dimen.margin_text_small)
            }
            relativeLayout {
                imageView {
                    protectedImage = this
                    backgroundColor = color(R.color.transparent)
                    image = drawable(R.drawable.ic_lock_outline_white_24dp)
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                }.lparams(dip(16), dip(16)) {
                    centerVertically()
                }
            }.lparams(wrapContent, wrapContent) {
                endOf(R.id.user_name_text)
                sameTop(R.id.user_name_text)
                sameBottom((R.id.user_name_text))
                alignParentEnd()
                marginStart = dimen(R.dimen.margin_medium)
            }

            textView {
                screenNameText = this
                id = R.id.screen_name_text
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                textSizeDimen = R.dimen.text_size_small
                textColor = color(resourceId(android.R.attr.textColorSecondary))
            }.lparams(wrapContent, wrapContent) {
                bottomOf(R.id.user_name_text)
                marginBottom = dimen(R.dimen.margin_text_medium)
            }

            textView {
                relationInfoText = this
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                textSizeDimen = R.dimen.text_size_small
                textColor = color(resourceId(android.R.attr.textColorSecondary))
            }.lparams(wrapContent, wrapContent) {
                endOf(R.id.screen_name_text)
                bottomOf(R.id.user_name_text)
                marginStart = dimen(R.dimen.margin_text_small)
                alignParentEnd()
            }

            textView {
                descriptionText = this
                id = R.id.description_text
                textSizeDimen = R.dimen.text_size_medium
                textColor = color(resourceId(android.R.attr.textColorPrimary))
            }.lparams(wrapContent, wrapContent) {
                bottomOf(R.id.screen_name_text)
                marginBottom = dimen(R.dimen.margin_text_small)
            }
            textView {
                locationText = this
                id = R.id.location_text
                textSizeDimen = R.dimen.text_size_medium
                ellipsize = TextUtils.TruncateAt.END
                maxLines = 1
                textColor = color(resourceId(android.R.attr.textColorPrimary))
            }.lparams(wrapContent, wrapContent) {
                bottomOf(R.id.description_text)
                marginBottom = dimen(R.dimen.margin_text_small)
            }

            textView {
                subInfoText = this
                textSizeDimen = R.dimen.text_size_small
                textColor = color(resourceId(android.R.attr.textColorSecondary))
            }.lparams(wrapContent, wrapContent) {
                bottomOf(R.id.location_text)
            }


        }.lparams(matchParent, matchParent) {
            margin = dimen(R.dimen.margin_medium)
            bottomOf(R.id.buttons_relative)
            alignParentStart()
        }
    }.collapsingToolbarlparams(matchParent, matchParent) {
        collapseMode = COLLAPSE_MODE_OFF
    }

    private fun AnkoContext<*>.userProfileHeader(manager: ViewManager) = manager.relativeLayout {
        id = R.id.header_relative
        headerImageView {
            id = R.id.header_image
            headerImage = this
            scaleType = ImageView.ScaleType.CENTER_CROP
        }.lparams(matchParent, wrapContent)
        view {
            this.background = drawable(R.drawable.header_image_gradation)
        }.lparams(matchParent, wrapContent) {
            sameBottom(R.id.header_image)
            sameTop(R.id.header_image)
        }
    }.collapsingToolbarlparams(wrapContent, wrapContent) {
        collapseMode = COLLAPSE_MODE_PIN
    }

    private fun AnkoContext<*>.userProfileIcon(manager: ViewManager) = manager.relativeLayout {
        id = R.id.icon_frame
        headerImageView {
            id = R.id.dummy_header2
            visibility = View.INVISIBLE
        }.lparams(matchParent, wrapContent)
        relativeLayout {
            iconRelative = this
            imageView {
                iconImage = this
                backgroundColor = color(R.color.image_background)
            }.lparams(dip(53), dip(53)) { centerInParent() }

            view {
                background = drawable(R.drawable.image_frame)
            }.lparams(dip(56), dip(56))
        }.lparams(wrapContent, wrapContent) {
            sameBottom(R.id.dummy_header2)
            marginBottom = dimen(R.dimen.margin_large)
        }
    }.collapsingToolbarlparams(wrapContent, wrapContent) {
        collapseMode = COLLAPSE_MODE_OFF
        margin = dimen(R.dimen.margin_medium)
        marginTop = dimen(R.dimen.app_bar_standard_height)
    }

    class HeaderImageView(ctx: Context) : ImageView(ctx) {
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            val widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightSize = widthSize / 3 //3:1
            setMeasuredDimension(widthSize, heightSize)
        }
    }

    private inline fun ViewManager.headerImageView(theme: Int = 0, init: HeaderImageView.() -> Unit): ImageView {
        return ankoView(::HeaderImageView, theme) { init() }
    }
}

interface IUserInfoActivityUI : AnkoComponent<UserInfoActivity> {
    val swipeRefresh: SwipeRefreshLayout
    val appBar: AppBarLayout
    val toolbar: Toolbar
    val collapsingToolbar: CollapsingToolbarLayout
    val headerImage: ImageView
    val iconImage: ImageView
    val iconRelative: RelativeLayout
    val infoRelative: RelativeLayout
    val userNameText: TextView
    val screenNameText: TextView
    val descriptionText: TextView
    val locationText: TextView
    val subInfoText: TextView
    val userProfileTabLayout: TabLayout
    val pager: ViewPager
    val followButton: ImageButton
    val protectedImage: ImageView
    val relationInfoText: TextView
    val profileEditButton: Button
}
