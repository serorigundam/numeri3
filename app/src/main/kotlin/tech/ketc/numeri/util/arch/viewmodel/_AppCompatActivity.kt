package tech.ketc.numeri.util.arch.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

inline fun <reified T : ViewModel> AppCompatActivity.viewModel(crossinline provider: () -> ViewModelProvider.Factory)
        = lazy { ViewModelProviders.of(this, provider()).get(T::class.java) }

inline fun <reified T : ViewModel> Fragment.viewModel(crossinline provider: () -> ViewModelProvider.Factory)
        = lazy { ViewModelProviders.of(this, provider()).get(T::class.java) }
