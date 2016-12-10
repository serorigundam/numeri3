package net.ketc.numeri

import android.app.Application
import net.ketc.numeri.domain.entity.entities
import net.ketc.numeri.util.ormlite.createTable


class Numeri : Application() {
    override fun onCreate() {
        super.onCreate()
        Injectors.setApplication(this)
        createTable(*entities)
    }
}
