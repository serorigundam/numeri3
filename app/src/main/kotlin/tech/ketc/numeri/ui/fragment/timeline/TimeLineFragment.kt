package tech.ketc.numeri.ui.fragment.timeline

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.support.v4.ctx
import tech.ketc.numeri.ui.components.ITimelineUIComponent
import tech.ketc.numeri.ui.components.TimelineUIComponent
import tech.ketc.numeri.ui.model.TimeLineViewModel
import tech.ketc.numeri.ui.view.recycler.timeline.TimeLineDataSourceAdapter
import tech.ketc.numeri.ui.view.recycler.timeline.TweetViewHolder
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import javax.inject.Inject

class TimeLineFragment : Fragment(), AutoInject, ITimelineUIComponent by TimelineUIComponent() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: TimeLineViewModel by viewModel { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = createView(ctx)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.clients.observe(this) {
            it.ifPresent {
                val client = it.firstOrNull() ?: return@ifPresent
                model.setClient(client)
                val adapter = TimeLineDataSourceAdapter(this, model.dataSource, { TweetViewHolder(ctx, client, this, model) })
                adapter.pageSize = 30
                timelineRecycler.adapter = adapter
                swipeRefresh.isEnabled = false
                swipeRefresh.isRefreshing = true
                adapter.loadInitial {
                    swipeRefresh.isEnabled = true
                    swipeRefresh.isRefreshing = false
                }
                swipeRefresh.setOnRefreshListener {
                    swipeRefresh.isRefreshing = true
                    adapter.loadAfter {
                        swipeRefresh.isRefreshing = false
                    }
                }
            }
        }
    }
}