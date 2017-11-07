package tech.ketc.numeri.util.arch.livedata

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.util.Log
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import tech.ketc.numeri.util.arch.livedata.response.Response

class MutableAsyncLiveData<S, T>(trigger: LiveData<S?>, private val transform: suspend (S?) -> T)
    : NonnullMediatorLiveData<Response<T>>(), Cancellable {

    private var job: Job? = null

    init {
        addSource(trigger, { asyncTransform(it) })
    }

    fun observe(owner: LifecycleOwner, onChanged: (Response<T>) -> Unit) {
        super.observe(owner, Observer { onChanged(it ?: throw IllegalStateException()) })
    }

    @SuppressWarnings
    override fun setValue(value: Response<T>) {
        super.setValue(value)
    }

    @SuppressWarnings
    override fun postValue(value: Response<T>) {
        super.postValue(value)
    }

    private fun asyncTransform(source: S?) {
        job?.cancel()
        job = null
        job = launch(UI) {
            val res = async(coroutineContext + CommonPool) {
                var result: T? = null
                var error: Throwable? = null
                try {
                    result = transform(source)
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
            setValue(res)
        }
    }

    override fun cancel() {
        job?.cancel()
        Log.v(javaClass.name,"cancel")
    }
}