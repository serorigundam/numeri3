package net.ketc.numeri.presentation.view.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import net.ketc.numeri.presentation.presenter.activity.Presenter

/**
 * @param P is bound [Presenter]
 */
abstract class ApplicationActivity<out P : Presenter<ActivityInterface>> : AppCompatActivity() {
    /**
     * this is bound activity-life-cycle
     */
    protected abstract val presenter: P

    override fun onPause() {
        super.onPause()
        presenter.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        presenter.onRestoreInstanceState(savedInstanceState)
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