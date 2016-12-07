package net.ketc.numeri

import android.app.Application


class Numeri : Application() {
    override fun onCreate() {
        super.onCreate()
        Injectors.setApplication(this)
    }

}
