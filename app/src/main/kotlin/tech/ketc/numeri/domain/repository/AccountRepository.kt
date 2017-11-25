package tech.ketc.numeri.domain.repository

import android.util.ArraySet
import tech.ketc.numeri.App
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.IOAuthSupportFactory
import tech.ketc.numeri.domain.twitter.ITwitterClientFactory
import tech.ketc.numeri.domain.twitter.twitterCallbackUrl
import tech.ketc.numeri.infra.AccountDatabase
import tech.ketc.numeri.infra.element.TlType
import tech.ketc.numeri.infra.entity.AccountToken
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.logTag
import tech.ketc.numeri.util.unmodifiableSet
import twitter4j.auth.OAuthSupport
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import kotlin.concurrent.read
import kotlin.concurrent.write

class AccountRepository @Inject constructor(private val mApp: App,
                                            private val mDatabase: AccountDatabase,
                                            private val mOAuthSupportFactory: IOAuthSupportFactory,
                                            private val mTwitterClientFactory: ITwitterClientFactory,
                                            private val mTimelineInfoRepository: ITimelineInfoRepository)
    : IAccountRepository {
    private var mOAuthSupport: OAuthSupport? = null
    private var mClients: MutableSet<TwitterClient>? = null
    private val mDao = mDatabase.accountDao()
    private val mLock = ReentrantReadWriteLock()

    init {
        Logger.v(logTag,"new instance")
    }

    override fun createAuthorizationURL(): String {
        val oAuthSupport = mOAuthSupportFactory.create()
        val oAuthRequestToken = oAuthSupport.getOAuthRequestToken(mApp.twitterCallbackUrl)
        val authorizationURL: String? = oAuthRequestToken.authorizationURL
        return authorizationURL?.also {
            mOAuthSupport = oAuthSupport
        } ?: throw OAuthFailureException()
    }

    override fun createTwitterClient(oauthVerifier: String): TwitterClient {
        fun create(): MutableSet<TwitterClient> {
            val set = ArraySet<TwitterClient>()
            mClients = set
            return set
        }

        return mLock.write {
            val oAuthSupport = mOAuthSupport ?: throw IllegalStateException("need to call the clients createAuthorizationURL()")
            val accessToken = oAuthSupport.getOAuthAccessToken(oauthVerifier)
            val accountToken = AccountToken(accessToken)
            mDao.insert(accountToken)
            val clients = mClients ?: create()
            val generate = clients.isEmpty()
            mTwitterClientFactory.create(accountToken).also {
                clients.add(it)
                if (generate)
                    createDefaultTimelineGroup(accountToken)
            }
        }
    }

    private fun createDefaultTimelineGroup(token: AccountToken) {
        val group = mTimelineInfoRepository.createGroup("Main")
        val accountId = token.id
        val home = mTimelineInfoRepository.getInfo(TlType.HOME, accountId)
        val mentions = mTimelineInfoRepository.getInfo(TlType.MENTIONS, accountId)
        mTimelineInfoRepository.joinToGroup(group, home)
        mTimelineInfoRepository.joinToGroup(group, mentions)
        mTimelineInfoRepository.notifyDataChanged()
    }


    private fun createClients() = mLock.read {
        val tokens = mDao.selectAll()
        tokens.map { mTwitterClientFactory.create(it) }.toSet().also {
            mClients = it.toMutableSet()
        }
    }

    override fun clients() = (mClients ?: createClients()).unmodifiableSet()


    override fun deleteClient(twitterClient: TwitterClient) {
        mLock.write {
            val clients = mClients
            clients ?: throw IllegalStateException("need to call the clients() or createTwitterClient()")
            if (clients.isEmpty()) throw IllegalStateException("TwitterClient is not registered")
            val deletionClient = clients.find { it.id == twitterClient.id }
                    ?: throw IllegalArgumentException("does not exist specified twitterClient")
            mDao.deleteById(deletionClient.id)
            clients.remove(deletionClient)
        }
    }
}