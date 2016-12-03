package net.ketc.numeri.rx.util

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

interface Task {
    infix fun error(error: (Throwable) -> Unit): Task
}

class SingleTask<T>(private val subscribeOn: Scheduler, private val observeOn: Scheduler, private val autoDisposable: AutoDisposable?, private val task: () -> T) : Task {
    private var success: ((T) -> Unit)? = null
    private var error: ((Throwable) -> Unit)? = null

    infix fun success(success: (T) -> Unit): Disposable {
        this.success = success
        val single = Single.create<T> { emitter ->
            val t = task()
            emitter.onSuccess(t)
        }

        val disposable = single.subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(success) {
                    error?.invoke(it) ?: throw it
                }
        autoDisposable?.let {
            autoDisposable.addDisposable(disposable)
        }
        return disposable
    }

    override infix fun error(error: (Throwable) -> Unit): SingleTask<T> {
        this.error = error
        return this
    }
}

fun <T> singleTask(scheduler: Scheduler, observeOn: Scheduler = AndroidSchedulers.mainThread(), autoDisposable: AutoDisposable? = null, task: () -> T) = SingleTask(scheduler, observeOn, autoDisposable, task)
