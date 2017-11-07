package tech.ketc.numeri.ui.main

import android.arch.lifecycle.ViewModelProvider
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import org.jetbrains.anko.setContentView
import tech.ketc.numeri.ui.model.MainViewModel
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import javax.inject.Inject

class MainActivity : AppCompatActivity(), AutoInject, IMainUI by MainUI() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: MainViewModel by viewModel { viewModelFactory }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        model.text.observe(this) {
            it.ifPresent { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
            it.ifError { it.printStackTrace() }
        }
    }
}