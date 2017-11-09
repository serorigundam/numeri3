package tech.ketc.numeri.infra.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import twitter4j.auth.AccessToken
import java.io.Serializable

@Entity(tableName = "account_token")
class AccountToken(
        @PrimaryKey()
        val id: Long = 0,
        @ColumnInfo(name = "auth_token")
        var authToken: String,
        @ColumnInfo(name = "auth_tokenSecret")
        var authTokenSecret: String) : Serializable {
    constructor(accessToken: AccessToken) : this(accessToken.userId, accessToken.token, accessToken.tokenSecret)
}