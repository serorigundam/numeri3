package tech.ketc.numeri.infra

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import tech.ketc.numeri.infra.dao.AccountDao
import tech.ketc.numeri.infra.entity.AccountToken

@Database(entities = arrayOf(AccountToken::class), version = 1)
abstract class AccountDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
}