package tech.ketc.numeri.util

import android.util.Log

object Logger {
    var debug = false
    private fun tag(tag: String) = "debug logger:$tag"

    fun v(tag: String, message: String) {
        if (debug) Log.v(tag(tag), message)
    }

    fun d(tag: String, message: String) {
        if (debug) Log.d(tag(tag), message)
    }

    fun e(tag: String, message: String) {
        if (debug) Log.e(tag(tag), message)
    }

    fun i(tag: String, message: String) {
        if (debug) Log.i(tag(tag), message)
    }

    fun w(tag: String, message: String) {
        if (debug) Log.w(tag(tag), message)
    }

    fun wtf(tag: String, message: String) {
        if (debug) Log.wtf(tag(tag), message)
    }
}

inline val <T : Any> T.logTag: String
    get() = javaClass.name
