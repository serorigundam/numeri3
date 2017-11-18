package tech.ketc.numeri.util.coroutine

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlin.coroutines.experimental.CoroutineContext

val CoroutineScope.asyncContext: CoroutineContext
    get() = coroutineContext + CommonPool

fun Job.dispose() {
    if (!isCompleted)
        cancel()

}