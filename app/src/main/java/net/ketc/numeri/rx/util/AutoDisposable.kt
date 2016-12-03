package net.ketc.numeri.rx.util

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.*

interface AutoDisposable {
    fun addDisposable(disposable: Disposable)
    fun removeDisposable(disposable: Disposable)
    fun disposeDisposable()
    fun Disposable.autoDispose() = addDisposable(this)
    /**
     * このメソッドを使用した場合[autoDispose]を呼ぶ必要はありません。
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

    override fun disposeDisposable() {
        disposableList.forEach(Disposable::dispose)
    }
}