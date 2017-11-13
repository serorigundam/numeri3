package tech.ketc.numeri.ui.view.recycler.timeline

import android.arch.lifecycle.LifecycleOwner
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.ui.view.recycler.DataSource
import tech.ketc.numeri.ui.view.recycler.DataSourceAdapter

class TimeLineDataSourceAdapter(owner: LifecycleOwner,
                                dataSource: DataSource<Long, Tweet>,
                                creator: () -> TweetViewHolder)
    : DataSourceAdapter<Long, Tweet, TweetViewHolder>(owner, dataSource, creator) {


    override fun onBindValueViewHolder(holder: TweetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}