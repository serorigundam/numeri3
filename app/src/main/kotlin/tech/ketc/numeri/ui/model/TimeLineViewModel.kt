package tech.ketc.numeri.ui.model

import android.arch.lifecycle.ViewModel
import tech.ketc.numeri.domain.repository.*
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.ui.model.delegate.ClientHandler
import tech.ketc.numeri.ui.model.delegate.IClientHandler
import tech.ketc.numeri.ui.model.delegate.IImageLoadable
import tech.ketc.numeri.ui.model.delegate.ImageLoadable
import tech.ketc.numeri.ui.view.recycler.timeline.TimeLineDataSource
import javax.inject.Inject

class TimeLineViewModel @Inject constructor(accountRepository: AccountRepository,
                                            userRepository: ITwitterUserRepository,
                                            imageRepository: IImageRepository,
                                            private val tweetRepository: ITweetRepository)
    : ViewModel(),
        IClientHandler by ClientHandler(accountRepository, userRepository),
        IImageLoadable by ImageLoadable(imageRepository) {

    private var mClient: TwitterClient? = null
        get() {
            return field ?: throw IllegalStateException()
        }

    fun setClient(client: TwitterClient) {
        mClient = client
    }

    val dataSource by lazy {
        TimeLineDataSource {
            mClient!!.twitter.getHomeTimeline(it)
                    .map { tweetRepository.createOrUpdate(it) }.toMutableList()
        }//todo 外から振る舞いを変えられるようにする
    }
}
