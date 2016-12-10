package net.ketc.numeri.domain.service

import android.content.Context
import net.ketc.numeri.Injectors
import net.ketc.numeri.R
import net.ketc.numeri.domain.entity.ClientToken
import net.ketc.numeri.domain.entity.createClientToken
import net.ketc.numeri.inject
import net.ketc.numeri.util.ormlite.dao
import net.ketc.numeri.util.ormlite.transaction
import twitter4j.TwitterException
import twitter4j.auth.AccessToken
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationContext
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
     * create twitter client and save token
     */
    fun createClient(oauthVerifier: String): TwitterClient

    /**
     * get saved clients
     */
    fun clients(): List<TwitterClient>
}

class OAuthServiceImpl : OAuthService {

    private var oAuthAuthorization: OAuthAuthorization? = null
    private val clients = ArrayList<TwitterClient>()
    private val reentrantLock: ReentrantLock = ReentrantLock()
    @Inject
    lateinit var applicationContext: Context

    private val callbackUrl by lazy {
        val scheme = applicationContext.getString(R.string.twitter_callback_scheme)
        val host = applicationContext.getString(R.string.twitter_callback_host)
        "$scheme://$host"
    }

    init {
        inject()
    }

    override fun createAuthorizationURL(): String {
        val configurationContext = ConfigurationContext.getInstance()
        val oAuthAuthorization = OAuthAuthorization(configurationContext).apply {
            val apiKey = applicationContext.getString(R.string.twitter_api_key)
            val secretKey = applicationContext.getString(R.string.twitter_secret_key)
            setOAuthConsumer(apiKey, secretKey)
        }
        val oAuthRequestToken = oAuthAuthorization.getOAuthRequestToken(callbackUrl)
        val authorizationURL: String? = oAuthRequestToken.authorizationURL
        authorizationURL?.let {
            this.oAuthAuthorization = oAuthAuthorization
            return it
        }
        throw TwitterException("failure to generate authorization URL")
    }

    override fun createClient(oauthVerifier: String): TwitterClient = transaction {
        val authorization = (oAuthAuthorization ?: throw IllegalStateException("authentication has not started"))
        val oAuthAccessToken: AccessToken = authorization.getOAuthAccessToken(oauthVerifier)
        val dao = dao(ClientToken::class)
        if (dao.queryForId(oAuthAccessToken.userId) != null)
            throw IllegalStateException("saved user")
        val clientToken = createClientToken(oAuthAccessToken)
        dao.create(clientToken)
        TwitterClient(applicationContext, clientToken).apply {
            clients.add(this)
        }
    }

    override fun clients(): List<TwitterClient> {
        return if (clients.isEmpty()) {
            reentrantLock.withLock {
                transaction {
                    val dao = dao(ClientToken::class)
                    val tokenList = dao.queryForAll()
                    clients.apply {
                        addAll(tokenList.map { TwitterClient(applicationContext, it) })
                    }
                }
            }
        } else {
            clients
        }
    }

}