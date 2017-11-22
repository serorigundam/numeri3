package tech.ketc.numeri.util.android.ui

import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.view.View

interface SnackbarMaker {
    fun getSnackSourceView(): View
    fun make(message: String, duration: Int = Snackbar.LENGTH_SHORT): Snackbar = Snackbar.make(getSnackSourceView(), message, duration)
    fun make(@StringRes stringResId: Int, duration: Int = Snackbar.LENGTH_SHORT): Snackbar = Snackbar.make(getSnackSourceView(), stringResId, duration)
}