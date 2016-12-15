package net.ketc.numeri.util.log

import android.util.Log

fun v(tag: String, msg: String): Unit {
    Log.v(tag, msg)
}

fun d(tag: String, msg: String): Unit {
    Log.d(tag, msg)
}

fun e(tag: String, msg: String): Unit {
    Log.e(tag, msg)
}

fun i(tag: String, msg: String): Unit {
    Log.i(tag, msg)
}

fun w(tag: String, msg: String): Unit {
    Log.w(tag, msg)
}

fun wtf(tag: String, msg: String): Unit {
    Log.wtf(tag, msg)
}