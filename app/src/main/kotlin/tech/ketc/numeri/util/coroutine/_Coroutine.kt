package tech.ketc.numeri.util.coroutine

import kotlinx.coroutines.experimental.Job

fun Job.dispose() {
    if (!isCompleted)
        cancel()

}