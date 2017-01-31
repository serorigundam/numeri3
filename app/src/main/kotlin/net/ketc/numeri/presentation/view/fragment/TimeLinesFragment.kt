package net.ketc.numeri.presentation.view.fragment

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentManager
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.ketc.numeri.R
import net.ketc.numeri.domain.entity.TweetsDisplayGroup
import net.ketc.numeri.presentation.presenter.fragment.TimeLinesPresenter
import net.ketc.numeri.presentation.view.component.TweetsDisplayPagerAdapter
import net.ketc.numeri.util.android.parent
import org.jetbrains.anko.below
import org.jetbrains.anko.design.tabLayout
import org.jetbrains.anko.dip
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.support.v4.viewPager

class TimeLinesFragment : ApplicationFragment<TimeLinesPresenter>(), TimeLinesFragmentInterface {
    override val presenter: TimeLinesPresenter = TimeLinesPresenter(this)

    override val activity: AppCompatActivity
        get() = this.parent

    override val group: TweetsDisplayGroup by lazy { arguments.getSerializable(EXTRA_GROUP) as TweetsDisplayGroup }

    override val fm: FragmentManager
        get() = childFragmentManager

    private var savedInstanceState: Bundle? = null

    private val viewPager: ViewPager by lazy { find<ViewPager>(R.id.pager) }
    private val tab: TabLayout by lazy { find<TabLayout>(R.id.tab) }
    private var adapter: TweetsDisplayPagerAdapter? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return createView(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.savedInstanceState = savedInstanceState
        super.onViewCreated(view, savedInstanceState)
        tab.setupWithViewPager(viewPager)
        presenter.initialize()
    }

    override fun onResume() {
        super.onResume()
        adapter?.onResume()
    }

    override fun setAdapter(adapter: TweetsDisplayPagerAdapter) {
        this.adapter = adapter
        viewPager.adapter = adapter
        if (savedInstanceState == null) {
            adapter.initialize()
        }
    }


    companion object {

        fun create(group: TweetsDisplayGroup) = TimeLinesFragment().apply {
            arguments = Bundle().apply {
                putSerializable(EXTRA_GROUP, group)
            }
        }

        val EXTRA_GROUP = "EXTRA_GROUP"

        private fun createView(ctx: Context): View = ctx.relativeLayout {
            lparams(matchParent, matchParent)
            tabLayout {
                id = R.id.tab
                elevation = dip(4).toFloat()
                tabMode = TabLayout.MODE_SCROLLABLE
            }.lparams(matchParent, dip(32))
            viewPager {
                id = R.id.pager
                offscreenPageLimit = 10
            }.lparams(matchParent, matchParent) {
                below(R.id.tab)
            }
        }
    }
}

interface TimeLinesFragmentInterface : FragmentInterface {
    val group: TweetsDisplayGroup
    val fm: FragmentManager
    fun setAdapter(adapter: TweetsDisplayPagerAdapter)
}