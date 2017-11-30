package tech.ketc.numeri.ui.view.recycler.timeline

import android.arch.lifecycle.LifecycleOwner
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.util.android.ui.recycler.DataSource
import tech.ketc.numeri.util.android.ui.recycler.DataSourceAdapter
import tech.ketc.numeri.util.copy

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
        store()?.value = items.copy()
        notifyItemInserted(0)
    }

    fun marge() {
        val items = items()
        val filtered = mStoredList.filter { tweet -> !items.any { it == tweet } }
        mStoredList.clear()
        items.addAll(0, filtered)
        notifyItemRangeInserted(0, filtered.size)
    }

    fun delete(tweet: Tweet) {
        val items = items()
        items.indexOfFirst { it.id == tweet.id }.takeIf { it != -1 }?.let { index ->
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}