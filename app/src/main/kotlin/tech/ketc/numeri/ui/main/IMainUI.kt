package tech.ketc.numeri.ui.main

import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import org.jetbrains.anko.AnkoComponent

interface IMainUI : AnkoComponent<MainActivity> {
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