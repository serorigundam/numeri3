package net.ketc.numeri.presentation.presenter.fragment.user.display

import net.ketc.numeri.presentation.view.fragment.UsersFragmentInterface
import twitter4j.PagableResponseList
import twitter4j.User

class FollowersPresenter(fragment: UsersFragmentInterface) : UsersPresenter(fragment) {
    override fun getUsers(): PagableResponseList<User> = client.twitter.getFollowersList(fragment.targetUserId, nextCursor, DEFAULT_COUNT)
}