package net.ketc.numeri

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import net.ketc.numeri.domain.entity.entities
import net.ketc.numeri.util.ormlite.createTable


class Numeri : Application() {
    override fun onCreate() {
        super.onCreate()
        Injectors.test = false
        cApplication = this
        createTable(*entities)
        val crashlytics = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()
        Fabric.with(this, crashlytics)
    }

    companion object {
        private var cApplication: Application? = null
        val application: Application
            get() = cApplication!!
    }
}
