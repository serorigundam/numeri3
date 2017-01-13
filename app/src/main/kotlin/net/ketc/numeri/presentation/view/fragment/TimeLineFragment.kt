package net.ketc.numeri.presentation.view.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.ketc.numeri.domain.entity.*
import net.ketc.numeri.presentation.presenter.fragment.TimeLinePresenter
import net.ketc.numeri.util.android.parent

class TimeLineFragment : ApplicationFragment<TimeLinePresenter>(), TimeLineFragmentInterface {
    override lateinit var presenter: TimeLinePresenter
    override val activity: Activity
        get() = this.parent

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val display = arguments.getSerializable(EXTRA_DISPLAY)
        presenter = when (display) {
            is TimeLineDisplay -> createTimeLinePresenter(display)
            is TweetsDisplay -> createTweetsPresenter(display)
            else -> throw InternalError()
        }
        return super.onCreateView(inflater, container, savedInstanceState)!!
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        presenter.initialize()
    }

    private fun createTimeLinePresenter(timeLineDisplay: TimeLineDisplay): TimeLinePresenter {
        return when (timeLineDisplay.type) {
            TimeLineType.HOME -> TODO()
            TimeLineType.MENTIONS -> TODO()
        }
    }

    private fun createTweetsPresenter(tweetsDisplay: TweetsDisplay): TimeLinePresenter {
        return when (tweetsDisplay.type) {
            TweetsDisplayType.USER_LIST -> TODO()
            TweetsDisplayType.PUBLIC -> TODO()
        }
    }

    override fun onPause() {
        super.onPause()
    }


    override fun onResume() {
        super.onResume()
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {
        val EXTRA_DISPLAY = "EXTRA_DISPLAY"

        fun create(display: Display) = TimeLineFragment().apply {
            arguments = Bundle().apply {
                putSerializable(EXTRA_DISPLAY, display)
            }
        }

    }

}

interface TimeLineFragmentInterface : FragmentInterface