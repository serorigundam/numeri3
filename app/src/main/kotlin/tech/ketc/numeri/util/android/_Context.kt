package tech.ketc.numeri.util.android

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.ctx
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

fun Activity.checkPermissions(permissionStr: String, requestCode: Int? = null, granted: () -> Unit) {
    ContextCompat.checkSelfPermission(this, permissionStr)
            .takeIf { it == PackageManager.PERMISSION_GRANTED }
            ?.let {
                granted()
            } ?: requestCode?.let { requestPermissions(arrayOf(permissionStr), it) }
}

fun Fragment.checkPermissions(permissionStr: String, requestCode: Int? = null, granted: () -> Unit) {
    ContextCompat.checkSelfPermission(ctx, permissionStr)
            .takeIf { it == PackageManager.PERMISSION_GRANTED }
            ?.let {
                granted()
            } ?: requestCode?.let { requestPermissions(arrayOf(permissionStr), it) }
}