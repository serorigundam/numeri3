package tech.ketc.numeri.util.android

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import tech.ketc.numeri.util.android.ui.SnackbarMaker

val Fragment.act: Activity
    get() = activity ?: parentFragment?.act ?: throw IllegalStateException("not connected to activity")

val Fragment.arg: Bundle
    get() = arguments ?: throw IllegalStateException("arguments is not set")

val Fragment.snackbarMaker: SnackbarMaker?
    get() = act as? SnackbarMaker