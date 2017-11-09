package tech.ketc.numeri.util.arch

import android.arch.lifecycle.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import tech.ketc.numeri.util.arch.response.Response

class BindingLifecycleAsyncTask<out T : Any>(private val task: suspend () -> T) {

    fun run(owner: LifecycleOwner, handle: (Response<T>) -> Unit) {
        val observer = TaskExecutor<T>()
        owner.lifecycle.addObserver(observer)
        observer.execute(task) {
            handle(it)
            owner.lifecycle.removeObserver(observer)
        }
    }

    class TaskExecutor<T : Any> : LifecycleObserver {
        private var isPropagatable = true
        private var isDestroy = false

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
            launch(UI) {
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
    }
}