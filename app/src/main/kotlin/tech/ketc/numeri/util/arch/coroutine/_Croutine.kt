package tech.ketc.numeri.util.arch.coroutine

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import tech.ketc.numeri.util.Logger
import java.lang.ref.WeakReference


fun bindLaunch(owner: LifecycleOwner, start: CoroutineStart = CoroutineStart.DEFAULT,
               block: suspend CoroutineScope.() -> Unit) = launch(UI, start, block).apply {
    val observer = createLifecycleObserver(this)
    val lifecycle = owner.lifecycle
    lifecycle.addObserver(observer)
    invokeOnCompletion { lifecycle.removeObserver(observer) }
}

private val tag = "tech.ketc.numeri.util.arch.coroutine.BindingLaunchObserver"

private fun createLifecycleObserver(job: Job) = object : LifecycleObserver {
    val mRef = WeakReference<Job>(job)

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        Logger.v(tag, "onDestroy()")
        val j = mRef.get() ?: return
        val completed = j.isCompleted
        Logger.v(tag, "job is completed:$completed")
        if (!completed) {
            Logger.v(tag, "job cancel")
            job.cancel()
        }
    }
}