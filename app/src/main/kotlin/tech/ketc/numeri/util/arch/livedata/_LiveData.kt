package tech.ketc.numeri.util.arch.livedata

import android.arch.lifecycle.*

open class NonnullLiveData<T : Any> : LiveData<T>()

open class NonnullMediatorLiveData<T : Any> : MediatorLiveData<T>()


fun <S, T : Any> LiveData<S?>.asyncMap(func: suspend (S?) -> T) = MutableAsyncLiveData(this, func)

fun <T, R> LiveData<T>.switchMap(func: (T) -> LiveData<R>): LiveData<R>
        = Transformations.switchMap(this, func)

fun <T, R> LiveData<T>.map(func: (T) -> R): LiveData<R> = Transformations.map(this, func)

fun <T> MutableLiveData<T>.mediate() = map { it }

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

