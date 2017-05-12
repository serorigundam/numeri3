package net.ketc.numeri.util.android

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.TypedValue

fun Context.getResourceId(resId: Int): Int {
    val outValue = TypedValue()
    theme.resolveAttribute(resId, outValue, true)
    return outValue.resourceId
}


/**
 * It checks whether the permission is granted, and if granted it performs processing.
 * requesting if not granted
 *
 * @param permissionStr permission string
 * @param requestCode request code of if permission is not granted
 * @param granted run if permission is granted
 */
fun Activity.checkPermissions(permissionStr: String, requestCode: Int? = null, granted: () -> Unit) {
    ContextCompat.checkSelfPermission(this, permissionStr)
            .takeIf { it == PackageManager.PERMISSION_GRANTED }
            ?.let {
                granted()
            } ?: requestCode?.let { requestPermissions(permissionStr, it) }
}

fun Activity.requestPermissions(permissionStr: String, requestCode: Int) {
    ActivityCompat.requestPermissions(this, arrayOf(permissionStr), requestCode)
}