package tech.ketc.numeri.util.android

import android.app.Activity
import android.support.v7.widget.Toolbar

inline fun Toolbar.setFinishWithNavigationClick(activity: Activity, crossinline pre: () -> Unit)
        = setNavigationOnClickListener { pre(); activity.finish() }

fun Toolbar.setFinishWithNavigationClick(activity: Activity)
        = setFinishWithNavigationClick(activity, {})