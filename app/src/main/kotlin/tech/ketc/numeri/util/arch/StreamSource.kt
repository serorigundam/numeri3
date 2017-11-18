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
    private val mStream by lazy { BindingLifecycleStreamInternal<T>() }

    fun post(value: T) {
        launch(UI) { mStream.onPost(value) }
    }

    fun stream(): BindingLifecycleStream<T> = mStream

    private class BindingLifecycleStreamInternal<T> : BindingLifecycleStream<T> {
        private val mStreams = ArrayList<BindStream<T>>()

        fun onPost(value: T) {
            Logger.v(logTag, "onPost")
            mStreams.forEach { it.post(value) }
        }

        override fun observe(owner: LifecycleOwner, handle: (T) -> Unit) {
            val stream = BindStream(handle)
            stream.destroy = {
                mStreams.remove(stream)
                owner.lifecycle.removeObserver(stream)
            }
            owner.lifecycle.addObserver(stream)
            mStreams.add(stream)
        }
    }

    class BindStream<in T>(private val handle: (T) -> Unit) : LifecycleObserver {
        private var mIsDestroy = false
        private var mIsActive = true
        private val mQueue = ArrayList<T>()
        var destroy: () -> Unit = {}
        private val mLock = ReentrantLock()

        fun post(value: T) = mLock.withLock {
            if (!mIsDestroy && mIsActive) handle(value)
            else mQueue.add(value).let { Logger.v(logTag, "queuing") }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() = mLock.withLock {
            mQueue.takeIf { it.isNotEmpty() }?.let { queue ->
                queue.forEach(handle)
                queue.clear()
            }
            mIsActive = true
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            mIsActive = false
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            mIsDestroy = true
            destroy()
        }
    }
}