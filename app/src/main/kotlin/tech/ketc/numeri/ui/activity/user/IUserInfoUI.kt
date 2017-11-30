package tech.ketc.numeri.ui.activity.user

import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.Toolbar
import android.widget.*
import org.jetbrains.anko.AnkoComponent

interface IUserInfoUI : AnkoComponent<UserInfoActivity> {
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
    val followInfoText:TextView
}