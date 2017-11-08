package tech.ketc.numeri.infra.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Query
import tech.ketc.numeri.infra.entity.AccountToken

@Dao
interface AccountDao : IDao<AccountToken> {
    @Query("SELECT * from account_token")
    fun selectAll(): List<AccountToken>

    @Query("SELECT * FROM account_token WHERE :id LIMIT 1")
    fun findById(id: Long): AccountToken

    @Query("DELETE FROM account_token WHERE id=:id")
    fun deleteById(id: Long)
}