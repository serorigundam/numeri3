package net.ketc.numeri.util.rx

import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

object MySchedulers {
    val stream = Schedulers.from(Executors.newFixedThreadPool(1))!!
    val io = Schedulers.from(Executors.newFixedThreadPool(1))!!
    val imageLoad = Schedulers.from(Executors.newFixedThreadPool(10))!!
    val twitter = Schedulers.from(Executors.newScheduledThreadPool(10))!!
}