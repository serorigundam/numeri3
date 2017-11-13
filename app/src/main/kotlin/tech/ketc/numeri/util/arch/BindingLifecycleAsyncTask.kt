package tech.ketc.numeri.util.arch

import android.arch.lifecycle.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import tech.ketc.numeri.util.arch.response.Response
import java.lang.ref.WeakReference

class BindingLifecycleAsyncTask<out T : Any>(private val task: suspend () -> T) {

    private val executor = TaskExecutor<T>()
    private var isExecuted = false
    private var mOwner: WeakReference<LifecycleOwner>? = null

    fun run(owner: LifecycleOwner, handle: (Response<T>) -> Unit) {
        if (isExecuted) throw IllegalStateException()
        isExecuted = true
        mOwner = WeakReference(owner)
        owner.lifecycle.addObserver(executor)
        executor.execute(task) {
            handle(it)
            owner.lifecycle.removeObserver(executor)
            mOwner = null
        }
    }

    fun cancel() {
        if (!isExecuted) throw IllegalStateException()
        val ownerRef = mOwner ?: return
        val owner = ownerRef.get() ?: return
        owner.lifecycle.removeObserver(executor.also { it.cancel() })
        mOwner = null
    }

    class TaskExecutor<T : Any> : LifecycleObserver {
        private var isPropagatable = true
        private var isDestroy = false
        private var job: Job? = null

        private val callbacks = ArrayList<(Response<T>) -> Unit>()

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            isPropagatable = true
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            isPropagatable = false
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            isDestroy = true
        }

        fun execute(task: suspend () -> T, callback: (Response<T>) -> Unit) {
            job = launch(UI) {
                val res = async(coroutineContext + CommonPool) {
                    var result: T? = null
                    var error: Throwable? = null
                    try {
                        result = task()
                    } catch (t: Throwable) {
                        error = t
                    }
                    object : Response<T> {
                        override val result: T
                            get() = result ?: throw IllegalStateException()
                        override val error: Throwable
                            get() = error ?: throw IllegalStateException()
                        override val isSuccessful: Boolean
                            get() = result != null
                    }
                }.await()
                if (isDestroy) return@launch
                if (isPropagatable) callback(res)
                else callbacks.add(callback)
            }
        }

        fun cancel() {
            job?.cancel()
        }
    }
}