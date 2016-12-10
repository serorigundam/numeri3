package net.ketc.numeri

import android.app.Application
import android.content.Context
import dagger.Component
import dagger.Module
import dagger.Provides
import net.ketc.numeri.domain.service.OAuthService
import net.ketc.numeri.domain.service.OAuthServiceImpl
import net.ketc.numeri.util.ormlite.DataBaseHelper
import javax.inject.Singleton

@Module
class AppModule(private val application: Application) {

    @Provides
    @Singleton
    fun provideApplicationContext(): Context = application.applicationContext
}

@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {
    fun inject(databaseHelper: DataBaseHelper)
    fun inject(oAuthService: OAuthServiceImpl)
}


//extension
fun DataBaseHelper.inject() {
    Injectors.appComponent.inject(this)
}

fun OAuthServiceImpl.inject() {
    Injectors.appComponent.inject(this)
}