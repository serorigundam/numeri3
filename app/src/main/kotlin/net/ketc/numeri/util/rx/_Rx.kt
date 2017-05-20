package net.ketc.numeri.util.rx

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import net.ketc.numeri.util.log.v

fun <T> Flowable<T>.twitterThread(): Flowable<T> = this.subscribeOn(MySchedulers.twitter).observeOn(AndroidSchedulers.mainThread())

fun <T> Flowable<T>.streamThread(): Flowable<T> = this.subscribeOn(MySchedulers.stream).observeOn(AndroidSchedulers.mainThread())

fun <T> Flowable<T>.subscribeNamed(onNext: (T) -> Unit = {},
                                   onError: (Throwable) -> Unit = { throw it },
                                   onComplete: () -> Unit = {}) = subscribe(onNext, onError, onComplete)!!

fun <T> Observable<T>.subscribeNamed(onNext: (T) -> Unit = {},
                                     onError: (Throwable) -> Unit = { throw it },
                                     onComplete: () -> Unit = {}) = subscribe(onNext, onError, onComplete)!!


fun <T> SingleEmitter<T>.safeSuccess(task: () -> T): T? {
    try {
        val t = task()
        this.onSuccess(t)
        return t
    } catch (ie: Exception) {
        val eName = ie.javaClass.name
        if (!this.isDisposed) {
            v("singleTask", "catch disposed : $eName")
            this.onError(ie)
        } else {
            //Avoiding bugs that occur when using OkHttpClient after dispose()
            v("singleTask", "catch : $eName")
        }
        return null
    }
}

fun <T> FlowableEmitter<T>.safeNext(task: () -> T): T? {
    try {
        val t = task()
        this.onNext(t)
        return t
    } catch (ie: Exception) {
        val eName = ie.javaClass.name
        if (!this.isCancelled) {
            v("flowable", "catch disposed : $eName")
            this.onError(ie)
        } else {
            //Avoiding bugs that occur when using OkHttpClient after dispose()
            v("flowable", "catch : $eName")
        }
        return null
    }
}

fun <T> ObservableEmitter<T>.safeNext(task: () -> T): T? {
    try {
        val t = task()
        this.onNext(t)
        return t
    } catch (ie: Exception) {
        val eName = ie.javaClass.name
        if (!this.isDisposed) {
            v("observable", "catch disposed : $eName")
            this.onError(ie)
        } else {
            //Avoiding bugs that occur when using OkHttpClient after dispose()
            v("observable", "catch : $eName")
        }
        return null
    }
}


