package net.ketc.numeri.presentation.view

import android.content.Context
import android.support.v7.app.AppCompatActivity
import net.ketc.numeri.presentation.presenter.Presenter

/**
 * @param P
 */
abstract class ApplicationActivity<P : Presenter<ActivityInterface>> : AppCompatActivity() {
    /**
     * this is bound activity-life-cycle
     */
    internal abstract var presenter: P

    override fun onPause() {
        super.onPause()
        presenter.onPause()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}

interface ActivityInterface {
    val ctx: Context
}