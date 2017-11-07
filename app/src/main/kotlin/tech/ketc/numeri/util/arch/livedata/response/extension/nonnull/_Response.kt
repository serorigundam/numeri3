package tech.ketc.numeri.util.arch.livedata.response.extension.nonnull

import tech.ketc.numeri.util.arch.livedata.response.Response

fun <T : Any> Response<T>.nullable(): T? = if (isSuccessful) result else null

inline fun <T : Any> Response<T>.orElseGet(supply: () -> T) = nullable() ?: supply()

fun <T : Any> Response<T>.orElse(other: T) = nullable() ?: other