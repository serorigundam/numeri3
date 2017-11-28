package tech.ketc.numeri.ui.activity.media

import android.support.design.widget.AppBarLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.View
import org.jetbrains.anko.AnkoComponent

interface IMediaUI : AnkoComponent<MediaActivity> {
    val componentRoot: View
    val toolbar: Toolbar
    val appBar: AppBarLayout
    val pager: ViewPager
}