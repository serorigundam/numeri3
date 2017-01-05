package net.ketc.numeri.domain.entity

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.util.ormlite.Entity
import twitter4j.auth.AccessToken

/**
 * Twitter user token
 */
@DatabaseTable
data class ClientToken(@DatabaseField(id = true)
                       override val id: Long = 0,
                       @DatabaseField(canBeNull = false)
                       val authToken: String = "",
                       @DatabaseField(canBeNull = false)
                       val authTokenSecret: String = "") : Entity<Long>

fun createClientToken(id: Long, authToken: String, authTokenSecret: String) = ClientToken(id, authToken, authTokenSecret)
fun createClientToken(authToken: AccessToken) = ClientToken(authToken.userId, authToken.token, authToken.tokenSecret)
fun TwitterClient.toClientToken(): ClientToken {
    val oAuthAccessToken = twitter.oAuthAccessToken
    return createClientToken(id, oAuthAccessToken.token, oAuthAccessToken.tokenSecret)
}