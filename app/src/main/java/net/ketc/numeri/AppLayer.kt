package net.ketc.numeri

import android.app.Application
import android.content.Context
import dagger.Component
import dagger.Module
import dagger.Provides
import net.ketc.numeri.ormlite.util.DataBaseHelper
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
}