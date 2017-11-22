package tech.ketc.numeri.ui.view.recycler.timeline

import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.util.android.ui.recycler.DataSource
import twitter4j.Paging

class TimeLineDataSource(private val mTweetsSupplier: (Paging) -> MutableList<Tweet>) : DataSource<Long, Tweet> {
    private fun paging(pageSize: Int, max: Long? = null, since: Long? = null): Paging {
        return Paging().apply {
            count = pageSize
            max?.let { maxId = max }
            since?.let { sinceId = since }
        }
    }

    override fun getKey(item: Tweet): Long = item.id

    override fun loadAfter(currentNewestKey: Long, pageSize: Int): List<Tweet>
            = mTweetsSupplier(paging(pageSize, since = currentNewestKey))

    override fun loadBefore(currentOldestKey: Long, pageSize: Int): List<Tweet>
            = mTweetsSupplier(paging(pageSize + 1, max = currentOldestKey))
            .also { if (it.isNotEmpty()) it.removeAt(0) }

    override fun loadInitial(pageSize: Int): List<Tweet>
            = mTweetsSupplier(paging(pageSize))
}