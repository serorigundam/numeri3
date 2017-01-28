package net.ketc.numeri.util.android

import android.app.Activity
import android.support.v4.app.Fragment

val Fragment.parent: Activity
    get() = activity ?: parentFragment?.parent ?: throw IllegalStateException("there is no instance of Fragment")