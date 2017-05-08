package net.ketc.numeri.presentation.presenter.fragment.user.display

import net.ketc.numeri.domain.inject
import net.ketc.numeri.domain.model.TwitterUser
import net.ketc.numeri.domain.model.UserRelation
import net.ketc.numeri.domain.model.cache.convert
import net.ketc.numeri.domain.service.OAuthService
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.presenter.fragment.AutoDisposableFragmentPresenter
import net.ketc.numeri.presentation.view.component.ReadableMore
import net.ketc.numeri.presentation.view.fragment.UsersFragmentInterface
import net.ketc.numeri.util.rx.MySchedulers
import twitter4j.PagableResponseList
import twitter4j.User
import javax.inject.Inject


abstract class UsersPresenter(override val fragment: UsersFragmentInterface)
    : AutoDisposableFragmentPresenter<UsersFragmentInterface>(), ReadableMore<MutableList<Pair<TwitterUser, UserRelation?>>> {

    @Inject
    lateinit var oAuthService: OAuthService
    protected val client: TwitterClient
        get() = mClient

    private lateinit var mClient: TwitterClient
    protected var nextCursor = -1L

    init {
        inject()
    }

    override fun initialize() {
        singleTask(MySchedulers.twitter) {
            oAuthService.clients()
        }.error {
            it.printStackTrace()
        } success { clients ->
            mClient = clients.first { it.id == fragment.clientId }
            fragment.setClient(client)
            initializeLoad()
        }
    }

    private fun initializeLoad() {
        singleTask(MySchedulers.twitter) {
            getUserRelationPairList()
        } error {
            it.printStackTrace()
        } success {
            fragment.setUserRelationPairList(it)
        }
    }

    private fun getUserRelationPairList(): MutableList<Pair<TwitterUser, UserRelation?>> {
        val pagable = getUsers()
        val users = pagable.map { it.convert() }
        val relationList = client.twitter
                .lookupFriendships(*users.map { it.id }.toLongArray())
                .map { it.convert(client) }
        nextCursor = pagable.nextCursor
        return users.map { user -> user to relationList.firstOrNull { user.id == it.targetUserId } }
                .toMutableList()
    }


    abstract fun getUsers(): PagableResponseList<User>

    override fun read(): MutableList<Pair<TwitterUser, UserRelation?>> = getUserRelationPairList()

    override fun error(throwable: Throwable) {
        throwable.printStackTrace()
    }

    override fun complete(t: MutableList<Pair<TwitterUser, UserRelation?>>) = fragment.setUserRelationPairList(t)

    companion object {
        val DEFAULT_COUNT = 100
    }

}