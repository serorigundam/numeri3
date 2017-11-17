package tech.ketc.numeri.util.arch

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.logTag
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class StreamSource<T> {
    private val stream by lazy { BindingLifecycleStreamInternal<T>() }

    fun post(value: T) {
        launch(UI) { stream.onPost(value) }
    }

    fun stream(): BindingLifecycleStream<T> = stream

    private class BindingLifecycleStreamInternal<T> : BindingLifecycleStream<T> {
        private val streams = ArrayList<BindStream<T>>()

        fun onPost(value: T) {
            Logger.v(logTag, "onPost")
            streams.forEach { it.post(value) }
        }

        override fun observe(owner: LifecycleOwner, handle: (T) -> Unit) {
            val stream = BindStream(handle)
            stream.destroy = {
                streams.remove(stream)
                owner.lifecycle.removeObserver(stream)
            }
            owner.lifecycle.addObserver(stream)
            streams.add(stream)
        }
    }

    class BindStream<in T>(private val handle: (T) -> Unit) : LifecycleObserver {
        private var isDestroy = false
        private var isActive = true
        private val mQueue = ArrayList<T>()
        var destroy: () -> Unit = {}
        private val lock = ReentrantLock()

        fun post(value: T) = lock.withLock {
            if (!isDestroy && isActive) handle(value)
            else mQueue.add(value).let { Logger.v(logTag, "queuing") }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() = lock.withLock {
            mQueue.takeIf { it.isNotEmpty() }?.let { queue ->
                queue.forEach(handle)
                queue.clear()
            }
            isActive = true
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            isActive = false
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            isDestroy = true
            destroy()
        }
    }
}