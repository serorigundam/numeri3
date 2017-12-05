package tech.ketc.numeri.ui.model

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import tech.ketc.numeri.domain.repository.IImageRepository
import tech.ketc.numeri.domain.repository.ITweetRepository
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.ui.model.delegate.IImageLoadable
import tech.ketc.numeri.ui.model.delegate.ITweetOperator
import tech.ketc.numeri.ui.model.delegate.ImageLoadable
import tech.ketc.numeri.ui.model.delegate.TweetOperator
import tech.ketc.numeri.ui.view.recycler.timeline.TimeLineDataSource
import twitter4j.Paging
import twitter4j.Query
import javax.inject.Inject

class SearchViewModel @Inject constructor(private val mTweetRepository: ITweetRepository,
                                          imageRepository: IImageRepository) : ViewModel(),
        ITweetOperator by TweetOperator(mTweetRepository),
        IImageLoadable by ImageLoadable(imageRepository) {

    companion object {
        private fun Paging.query(queryStr: String) = Query(queryStr).also { query ->
            maxId.takeIf { it != -1L }?.let { query.maxId = it }
            sinceId.takeIf { it != -1L }?.let { query.sinceId = it }
            query.count = count
            query.resultType = Query.ResultType.recent
        }

        private fun createDataSourceDelegate(client: TwitterClient,
                                             query: String,
                                             repo: ITweetRepository) = { paging: Paging ->
            client.twitter.search(paging.query(query)).tweets
                    .map { repo.createOrUpdate(client, it, true) }
                    .toMutableList()
        }
    }

    val dataSource: TimeLineDataSource
        get() = mDataSource ?: throw IllegalStateException()
    private var mDataSource: TimeLineDataSource? = null
    val storeLiveData = MutableLiveData<List<Tweet>>()

    fun initialize(client: TwitterClient, query: String) {
        mDataSource = TimeLineDataSource(createDataSourceDelegate(client, query, mTweetRepository))
    }
}
