package net.ketc.numeri

import android.app.Application
import net.ketc.numeri.domain.entity.entities
import net.ketc.numeri.util.ormlite.createTable


class Numeri : Application() {
    override fun onCreate() {
        super.onCreate()
        cApplication = this
        createTable(*entities)
    }

    companion object {
        private var cApplication: Application? = null
        val application: Application
            get() = cApplication!!
    }
}
