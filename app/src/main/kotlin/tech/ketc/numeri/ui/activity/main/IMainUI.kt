package tech.ketc.numeri.ui.activity.main

import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import org.jetbrains.anko.AnkoComponent
import tech.ketc.numeri.util.anko.UIComponent

interface IMainUI : AnkoComponent<MainActivity> {
    val toolbar: Toolbar
    val drawer: DrawerLayout
    val navigation: NavigationView
    val columnGroupWrapper: CoordinatorLayout
    val tweetFab: FloatingActionButton
    val accountListUI: IAccountListUI
    val navigationHeaderUI: INavigationHeaderUI
    val groupChangeFab: FloatingActionButton

    interface IAccountListUI : UIComponent<RelativeLayout> {
        val accountList: ViewGroup
        val addAccountButton: View
    }

    interface INavigationHeaderUI : UIComponent<RelativeLayout> {
        val navigationStateIndicator: ImageView
        val toggleNavigationStateButton: View
    }
}