package tech.ketc.numeri.util.arch.livedata

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations

fun <S, T : Any> LiveData<S?>.asyncSwitchMap(func: suspend (S?) -> T) = MutableAsyncLiveData(this, func)

fun <T, R> LiveData<T>.switchMap(func: (T) -> LiveData<R>): LiveData<R>
        = Transformations.switchMap(this, func)

fun <T, R> LiveData<T>.map(func: (T) -> R): LiveData<R> = Transformations.map(this, func)


open class NonnullLiveData<T : Any> : LiveData<T>()


open class NonnullMediatorLiveData<T : Any> : MediatorLiveData<T>()

inline fun <T> LiveData<T>.observe(owner: LifecycleOwner, crossinline func: (T?) -> Unit)
        = observe(owner, Observer { func(it) })

inline fun <T> LiveData<T>.observeNonnullOnly(owner: LifecycleOwner, crossinline func: (T) -> Unit)
        = observe(owner) { if (it != null) func(it) }

inline fun <T> LiveData<T>.observeIf(owner: LifecycleOwner,
                                     crossinline predicate: (T?) -> Boolean, crossinline func: (T?) -> Unit)
        = observe(owner) { if (predicate(it)) func(it) }

inline fun <T> LiveData<T>.observeIfNonnullOnly(owner: LifecycleOwner,
                                                crossinline predicate: (T) -> Boolean, crossinline func: (T) -> Unit)
        = observeNonnullOnly(owner) { if (predicate(it)) func(it) }

