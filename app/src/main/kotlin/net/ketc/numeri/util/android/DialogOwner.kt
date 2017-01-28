package net.ketc.numeri.util.android

import android.support.v7.app.AppCompatDialog

class DialogOwner {
    private var dialog: AppCompatDialog? = null

    fun showDialog(dialog: AppCompatDialog) {
        if (!(this.dialog?.isShowing ?: false)) {
            this.dialog = dialog
            dialog.show()
        }
    }

    fun onPause() {
        dialog?.let {
            if (it.isShowing) {
                it.hide()
            } else {
                dialog = null
            }
        }
    }

    fun onResume() {
        dialog?.let(AppCompatDialog::show)
    }

    fun onDestroy() {
        dialog?.let(AppCompatDialog::dismiss)
    }

}