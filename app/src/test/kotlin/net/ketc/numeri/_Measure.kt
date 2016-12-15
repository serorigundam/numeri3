package net.ketc.numeri

import kotlin.system.measureNanoTime

inline fun <R> printNanoTime(message: String = "", block: () -> R): R {
    var r: R? = null
    measureNanoTime {
        r = block()
    }.run { println("$message : ${this}ns ${this / 1000000000.toDouble()}s") }
    return r!!
}