package tech.ketc.numeri.util

import android.util.Log

object Logger {
    var debug = false

    fun v(tag: String, message: String) {
        if (debug) Log.v(tag, message)
    }

    fun d(tag: String, message: String) {
        if (debug) Log.d(tag, message)
    }

    fun e(tag: String, message: String) {
        if (debug) Log.e(tag, message)
    }

    fun i(tag: String, message: String) {
        if (debug) Log.i(tag, message)
    }

    fun w(tag: String, message: String) {
        if (debug) Log.w(tag, message)
    }

    fun wtf(tag: String, message: String) {
        if (debug) Log.wtf(tag, message)
    }

}
