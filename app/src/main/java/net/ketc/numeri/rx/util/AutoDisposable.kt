package net.ketc.numeri.rx.util

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.*

interface AutoDisposable {
    fun addDisposable(disposable: Disposable)
    fun removeDisposable(disposable: Disposable)
    fun dispose()
    fun Disposable.autoDispose() = addDisposable(this)
    /**
     * You do not need to call [autoDispose] when using this method.
     */
    fun <T> singleTask(scheduler: Scheduler, observeOn: Scheduler = AndroidSchedulers.mainThread(), task: () -> T) = SingleTask(scheduler, observeOn, this, task)

}

fun Disposable.autoDispose(autoDisposable: AutoDisposable) = autoDisposable.addDisposable(this)

class AutoDisposableImpl : AutoDisposable {
    private val disposableList = ArrayList<Disposable>()
    override fun addDisposable(disposable: Disposable) {
        if (!disposableList.contains(disposable)) {
            disposableList.add(disposable)
        }
    }

    override fun removeDisposable(disposable: Disposable) {
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
        disposableList.remove(disposable)
    }

    override fun dispose() {
        disposableList.forEach(Disposable::dispose)
    }
}