package tech.ketc.numeri.ui.activity.timeline

import android.arch.lifecycle.ViewModelProvider
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import tech.ketc.numeri.util.di.AutoInject
import javax.inject.Inject

class TimelineManageActivity : AppCompatActivity(), HasSupportFragmentInjector, AutoInject {
    @Inject lateinit var androidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    //impl interface
    override fun supportFragmentInjector(): AndroidInjector<Fragment> = androidInjector
}