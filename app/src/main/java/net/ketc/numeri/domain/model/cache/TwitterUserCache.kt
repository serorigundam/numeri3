package net.ketc.numeri.domain.model.cache

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.Disposables
import net.ketc.numeri.domain.model.TwitterUser
import net.ketc.numeri.domain.model.TwitterUserImpl
import twitter4j.User
import java.util.*

object TwitterUserCache : ConversionCache<User, TwitterUser, Long> {
    private val map = LinkedHashMap<Long, TwitterUserImpl>()
    private var updateCallback: (TwitterUser) -> Unit = { user ->
        updateCallbacks.forEach { it(user) }
    }

    private val updateCallbacks = ArrayList<(TwitterUser) -> Unit>()

    override fun get(id: Long): TwitterUser? = map[id]
    override fun put(obj: User): TwitterUser {
        val id = obj.id
        val twitterUser = map[id]
        return if (twitterUser == null) {
            val twitterUserImpl = TwitterUserImpl(obj)
            twitterUserImpl.updateCallback = updateCallback
            map.put(id, twitterUserImpl)
            twitterUserImpl
        } else {
            twitterUser.update(obj)
            twitterUser
        }
    }

    val userUpdateFlowable = Flowable.create<TwitterUser>({ emitter ->
        val callback: (TwitterUser) -> Unit = {
            emitter.onNext(it)
        }
        updateCallbacks.add(callback)
        emitter.setDisposable(Disposables.fromAction {
            updateCallbacks.remove(callback)
        })
    }, BackpressureStrategy.BUFFER)

}