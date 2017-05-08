package net.ketc.numeri.presentation.presenter.component

import io.reactivex.disposables.Disposable
import net.ketc.numeri.domain.model.RelationType
import net.ketc.numeri.domain.model.TwitterUser
import net.ketc.numeri.domain.model.UserRelation
import net.ketc.numeri.domain.model.cache.convert
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.view.component.UserViewHolder
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.rx.MySchedulers

class TwitterUserViewPresenter(private val userViewHolder: UserViewHolder, autoDisposable: AutoDisposable, private val client: TwitterClient) : AutoDisposable by autoDisposable {

    fun updateState(userRelation: UserRelation): Disposable {
        userViewHolder.followButton.isEnabled = false
        return singleTask(MySchedulers.twitter) {
            val twitter = client.twitter
            when (userRelation.type) {
                RelationType.MUTUAL, RelationType.FOLLOWING -> twitter.destroyFriendship(userRelation.targetUserId)
                RelationType.FOLLOWED, RelationType.NOTHING -> twitter.createFriendship(userRelation.targetUserId)
                else -> throw IllegalStateException()
            }
            twitter.showFriendship(client.id, userRelation.targetUserId).convert()
        } error {
            it.printStackTrace()
            userViewHolder.followButton.isEnabled = true
        } success {
            userViewHolder.setState(userRelation)
            userViewHolder.followButton.isEnabled = true
        }
    }
}