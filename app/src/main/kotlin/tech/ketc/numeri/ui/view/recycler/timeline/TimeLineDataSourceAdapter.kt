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

    private val mStoredList = ArrayList<Tweet>()

    fun store(value: Tweet) {
        mStoredList.add(0, value)
    }

    fun insertTop(value: Tweet) {
        val items = items()
        if (items.any { it == value }) return
        items.add(0, value)
        notifyItemInserted(0)
    }

    fun marge() {
        val items = items()
        val filtered = mStoredList.filter { tweet -> !items.any { it == tweet } }
        mStoredList.clear()
        items.addAll(0, filtered)
        notifyItemRangeInserted(0, filtered.size)
    }
}