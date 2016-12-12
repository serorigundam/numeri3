package net.ketc.numeri.domain.service

import net.ketc.numeri.domain.entity.ClientToken
import net.ketc.numeri.domain.entity.createClientToken
import net.ketc.numeri.inject
import net.ketc.numeri.util.twitter.TwitterApp
import net.ketc.numeri.util.ormlite.dao
import net.ketc.numeri.util.ormlite.transaction
import net.ketc.numeri.util.toImmutableList
import net.ketc.numeri.util.twitter.OAuthSupportFactory
import twitter4j.TwitterException
import twitter4j.auth.AccessToken
import twitter4j.auth.OAuthSupport
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

interface OAuthService {
    /**
     * create twitter create authorization URL
     */
    fun createAuthorizationURL(): String

    /**
     * create [TwitterClientImpl] and save to database ,add to this service
     */
    fun createTwitterClient(oauthVerifier: String): TwitterClient


    /**
     * create or get clients from saved [ClientToken]
     */
    fun clients(): List<TwitterClient>

    /**
     * delete client
     */
    fun deleteClient(twitterClient: TwitterClient)
}

class OAuthServiceImpl : OAuthService {
    private var oAuthSupport: OAuthSupport? = null
    private val clients = ArrayList<TwitterClient>()
    private val reentrantLock: ReentrantLock = ReentrantLock()
    @Inject
    lateinit var twitterApp: TwitterApp
    @Inject
    lateinit var oAuthSupportFactory: OAuthSupportFactory

    init {
        inject()
    }

    override fun createAuthorizationURL(): String {
        val oAuthSupport = oAuthSupportFactory.create().apply {
            setOAuthConsumer(twitterApp.apiKey, twitterApp.apiSecret)
        }
        val oAuthRequestToken = oAuthSupport.getOAuthRequestToken(twitterApp.callbackUrl)
        val authorizationURL: String? = oAuthRequestToken.authorizationURL
        authorizationURL?.let {
            this.oAuthSupport = oAuthSupport
            return it
        }
        throw TwitterException("failure to generate authorization URL")
    }

    override fun createTwitterClient(oauthVerifier: String): TwitterClient = transaction {
        val oAuthSupport = (this.oAuthSupport ?: throw IllegalStateException("authentication has not started"))
        val oAuthAccessToken: AccessToken = oAuthSupport.getOAuthAccessToken(oauthVerifier)
        val dao = dao(ClientToken::class)
        if (dao.queryForId(oAuthAccessToken.userId) != null)
            throw IllegalStateException("saved user")
        val token = createClientToken(oAuthAccessToken).apply {
            dao.create(this)
        }
        TwitterClientImpl(twitterApp, token).apply { clients.add(this) }
    }

    override fun clients(): List<TwitterClient> {
        if (clients.isNotEmpty()) {
            return clients.toImmutableList()
        }
        return initializeClients().toImmutableList()
    }

    override fun deleteClient(twitterClient: TwitterClient) {
        reentrantLock.withLock {
            clients.remove(twitterClient)
            val dao = dao(ClientToken::class)
            dao.deleteById(twitterClient.id)
        }
    }

    private fun initializeClients(): List<TwitterClient> = reentrantLock.withLock {
        transaction {
            val dao = dao(ClientToken::class)
            val tokenList = dao.queryForAll()
            clients.apply {
                addAll(tokenList.map {
                    TwitterClientImpl(twitterApp, it)
                })
            }
        }
    }
}