package tech.ketc.numeri.util.android

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.TypedValue

fun Context.getResourceId(resId: Int): Int {
    val outValue = TypedValue()
    theme.resolveAttribute(resId, outValue, true)
    return outValue.resourceId
}

val Context.pref: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)