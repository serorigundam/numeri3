package net.ketc.numeri

import android.app.Application
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import net.ketc.numeri.domain.entity.entities
import net.ketc.numeri.util.ormlite.createTable


class Numeri : Application() {
    override fun onCreate() {
        super.onCreate()
        Injectors.test = false
        cApplication = this
        createTable(*entities)
        Fabric.with(this, Crashlytics())
    }

    companion object {
        private var cApplication: Application? = null
        val application: Application
            get() = cApplication!!
    }
}
