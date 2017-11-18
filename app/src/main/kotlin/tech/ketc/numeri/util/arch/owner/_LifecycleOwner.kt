package tech.ketc.numeri.util.arch.owner

import android.arch.lifecycle.LifecycleOwner
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.CoroutineStart
import tech.ketc.numeri.util.arch.coroutine.bindLaunch

fun LifecycleOwner.bindLaunch(start: CoroutineStart = CoroutineStart.DEFAULT, block: suspend CoroutineScope.() -> Unit)
        = bindLaunch(this, start, block)