package net.ketc.numeri.util.rx

import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers

fun <T> Flowable<T>.twitterThread(): Flowable<T> = this.subscribeOn(MySchedulers.twitter).observeOn(AndroidSchedulers.mainThread())

fun <T> Flowable<T>.streamThread(): Flowable<T> = this.subscribeOn(MySchedulers.stream).observeOn(AndroidSchedulers.mainThread())

