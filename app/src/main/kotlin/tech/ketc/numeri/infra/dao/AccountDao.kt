package tech.ketc.numeri.infra.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import tech.ketc.numeri.infra.entity.AccountToken

@Dao
interface AccountDao : IDao<AccountToken> {
    @Query("SELECT * from account_token")
    fun getAll(): List<AccountToken>

    @Query("SELECT * FROM account_token WHERE :id")
    fun findById(id: Long): AccountToken
}