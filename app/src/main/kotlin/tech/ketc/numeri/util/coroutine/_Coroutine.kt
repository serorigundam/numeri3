package tech.ketc.numeri.util.coroutine

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineScope
import kotlin.coroutines.experimental.CoroutineContext

val CoroutineScope.asyncContext: CoroutineContext
    get() = coroutineContext + CommonPool