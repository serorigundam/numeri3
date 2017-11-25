package tech.ketc.numeri.ui.activity.tweet

import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.startActivity
import tech.ketc.numeri.util.di.AutoInject
import javax.inject.Inject

class TweetActivity : AppCompatActivity(), AutoInject, ITweetUI by TweetUI() {
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory

    companion object {
        fun start(ctx: Context) {
            ctx.startActivity<TweetActivity>()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
    }
}