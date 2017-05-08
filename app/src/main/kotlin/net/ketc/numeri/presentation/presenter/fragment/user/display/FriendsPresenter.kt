package net.ketc.numeri.presentation.presenter.fragment.user.display

import net.ketc.numeri.presentation.view.fragment.UsersFragmentInterface
import twitter4j.PagableResponseList
import twitter4j.User

class FriendsPresenter(fragment: UsersFragmentInterface) : UsersPresenter(fragment) {
    override fun getUsers(): PagableResponseList<User> = client.twitter.getFriendsList(fragment.targetUserId, nextCursor, DEFAULT_COUNT)
}
