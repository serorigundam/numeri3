package net.ketc.numeri.util.rx

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.UndeliverableException
import net.ketc.numeri.util.log.v
import java.io.InterruptedIOException

/**
 *SingleTask
 */
class SingleTask<T>(private val subscribeOn: Scheduler, private val observeOn: Scheduler, private val autoDisposable: AutoDisposable?, private val task: () -> T) {
    private var error: ((Throwable) -> Unit)? = null

    /**
     * sets the callback function called when the task succeeds.
     * Then execute the task.
     * @param success callback function called when the task succeeds.
     * @return Disposable
     */
    infix fun success(success: (T) -> Unit): Disposable {
        val disposable = Single.create<T> { emitter ->
            try {
                val t = task()
                emitter.onSuccess(t)
            } catch (ie: Exception) {
                val eName = ie.javaClass.name
                if (!emitter.isDisposed) {
                    v("singleTask", "catch disposed : $eName")
                    emitter.onError(ie)
                } else {
                    v("singleTask", "catch : $eName")
                }
            }
        }.subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(success) {
                    error?.invoke(it) ?: throw it
                }
        autoDisposable?.let {
            autoDisposable.addDisposable(disposable)
        }
        return disposable
    }

    /**
     * sets the callback function called when an error occurs.
     * @param error function called when an error occurs.
     * @return An instance of itself.
     */
    infix fun error(error: (Throwable) -> Unit): SingleTask<T> {
        this.error = error
        return this
    }
}

fun <T> singleTask(scheduler: Scheduler, observeOn: Scheduler = AndroidSchedulers.mainThread(), autoDisposable: AutoDisposable? = null, task: () -> T) = SingleTask(scheduler, observeOn, autoDisposable, task)
