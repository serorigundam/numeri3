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
import tech.ketc.numeri.util.unmodifiableSet
import twitter4j.auth.OAuthSupport
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import kotlin.concurrent.read
import kotlin.concurrent.write

class AccountRepository @Inject constructor(private val app: App,
                                            private val db: AccountDatabase,
                                            private val oAuthSupportFactory: IOAuthSupportFactory,
                                            private val twitterClientFactory: ITwitterClientFactory,
                                            private val timelineInfoRepository: ITimelineInfoRepository)
    : IAccountRepository {
    private var mOAuthSupport: OAuthSupport? = null
    private var mClients: MutableSet<TwitterClient>? = null
    private val dao = db.accountDao()
    private val lock = ReentrantReadWriteLock()

    override fun createAuthorizationURL(): String {
        val oAuthSupport = oAuthSupportFactory.create()
        val oAuthRequestToken = oAuthSupport.getOAuthRequestToken(app.twitterCallbackUrl)
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

        return lock.write {
            val oAuthSupport = mOAuthSupport ?: throw IllegalStateException("need to call the clients createAuthorizationURL()")
            val accessToken = oAuthSupport.getOAuthAccessToken(oauthVerifier)
            val accountToken = AccountToken(accessToken)
            dao.insert(accountToken)
            val clients = mClients ?: create()
            val generate = clients.isEmpty()
            twitterClientFactory.create(accountToken).also {
                clients.add(it)
                if (generate)
                    createDefaultTimelineGroup(accountToken)
            }
        }
    }

    private fun createDefaultTimelineGroup(token: AccountToken) {
        val group = timelineInfoRepository.createGroup("Main")
        val accountId = token.id
        val home = timelineInfoRepository.getInfo(TlType.HOME, accountId)
        val mentions = timelineInfoRepository.getInfo(TlType.MENTIONS, accountId)
        timelineInfoRepository.joinToGroup(group, home)
        timelineInfoRepository.joinToGroup(group, mentions)
        timelineInfoRepository.notifyDataChanged()
    }


    private fun createClients() = lock.read {
        val tokens = dao.selectAll()
        tokens.map { twitterClientFactory.create(it) }.toSet().also {
            mClients = it.toMutableSet()
        }
    }

    override fun clients() = (mClients ?: createClients()).unmodifiableSet()


    override fun deleteClient(twitterClient: TwitterClient) {
        lock.write {
            val clients = mClients
            clients ?: throw IllegalStateException("need to call the clients() or createTwitterClient()")
            if (clients.isEmpty()) throw IllegalStateException("TwitterClient is not registered")
            val deletionClient = clients.find { it.id == twitterClient.id }
                    ?: throw IllegalArgumentException("does not exist specified twitterClient")
            dao.deleteById(deletionClient.id)
            clients.remove(deletionClient)
        }
    }
}