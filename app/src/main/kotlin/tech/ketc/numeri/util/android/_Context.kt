package tech.ketc.numeri.util.android

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.TypedValue
import org.jetbrains.anko.startActivity
import tech.ketc.numeri.R

fun Context.getResourceId(resId: Int): Int {
    val outValue = TypedValue()
    theme.resolveAttribute(resId, outValue, true)
    return outValue.resourceId
}

val Context.pref: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)

inline fun <reified T : Activity> Activity.startLeftOut(vararg params: Pair<String, Any?>) {
    startActivity<T>(*params)
    overridePendingTransition(R.anim.right_in, R.anim.left_out)
}