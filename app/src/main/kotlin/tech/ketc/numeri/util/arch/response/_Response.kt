package tech.ketc.numeri.util.arch.response

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlin.coroutines.experimental.CoroutineContext

fun <T : Any> Response<T>.nullable(): T? = if (isSuccessful) result else null

inline fun <T : Any> Response<T>.orElseGet(supply: () -> T) = nullable() ?: supply()

fun <T : Any> Response<T>.orElse(other: T) = nullable() ?: other

inline fun <T : Any> Response<T>.orError(error: (Throwable) -> Unit) = nullable().also {
    if (!isSuccessful) error(this.error)
}

fun <R : Any> response(func: () -> R): Response<R> {
    var result: R? = null
    var throwable: Throwable? = null
    try {
        result = func()
    } catch (t: Throwable) {
        throwable = t
    }
    return object : Response<R> {
        override val result: R
            get() = result ?: throw throwable!!
        override val error: Throwable
            get() = throwable ?: throw IllegalStateException()
        override val isSuccessful: Boolean
            get() = result != null
    }
}

fun <R : Any> deferredRes(context: CoroutineContext, func: suspend () -> R): Deferred<Response<R>> = async(context) {
    var result: R? = null
    var throwable: Throwable? = null
    try {
        result = func()
    } catch (t: Throwable) {
        throwable = t
    }
    object : Response<R> {
        override val result: R
            get() = result ?: throw throwable!!
        override val error: Throwable
            get() = throwable ?: throw IllegalStateException()
        override val isSuccessful: Boolean
            get() = result != null
    }
}