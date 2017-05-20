package net.ketc.numeri.presentation.view.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import net.ketc.numeri.activityManger
import net.ketc.numeri.presentation.presenter.activity.Presenter
import net.ketc.numeri.presentation.presenter.activity.PresenterFactory
import net.ketc.numeri.util.log.v
import java.util.*

/**
 * @param P is bound [Presenter]
 */
abstract class ApplicationActivity<out P : Presenter<*>> : AppCompatActivity() {
    /**
     * this is bound activity-life-cycle
     */
    private var mPresenter: P? = null
    protected val presenter: P
        get() = mPresenter ?: throw IllegalStateException()
    protected abstract val presenterFactory: PresenterFactory<P>

    private var mActivityId: UUID? = null
    val activityId: UUID
        get() = mActivityId ?: throw IllegalStateException("activity not created")

    private var mIsStartedForFirst: Boolean? = null

    val isStartedForFirst: Boolean
        get() = mIsStartedForFirst ?: throw IllegalStateException("activity not created")

    val tag: String by lazy { activityId.toString() }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityManger = application.activityManger
        mActivityId = activityManger.getActivityId(this)
        mIsStartedForFirst = activityManger.checkStartedForFirst(activityId)
        mPresenter = presenterFactory.createOrGet(activityId)
        v(javaClass.simpleName, "onCreate uuid : $mActivityId, startedForFirst : $mIsStartedForFirst")
    }

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
        presenter.onDestroy(isFinishing)
        super.onDestroy()
    }
}

interface ActivityInterface {
    val ctx: Context
}