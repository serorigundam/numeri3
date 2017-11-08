package tech.ketc.numeri.util.arch.response.extension.nullable

import tech.ketc.numeri.util.arch.response.Response


inline fun <T : Any?> Response<T>.orElseGet(supply: () -> T) = result ?: supply()

fun <T : Any?> Response<T>.orElse(other: T) = result ?: other