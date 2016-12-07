package net.ketc.numeri.util.rx

import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

object MySchedulers {
    val stream = io.reactivex.schedulers.Schedulers.from(Executors.newFixedThreadPool(1))!!
    val io = io.reactivex.schedulers.Schedulers.from(Executors.newFixedThreadPool(1))!!
    val imageLoad = io.reactivex.schedulers.Schedulers.from(Executors.newFixedThreadPool(10))!!
    val twitter = io.reactivex.schedulers.Schedulers.from(Executors.newFixedThreadPool(10))!!
}