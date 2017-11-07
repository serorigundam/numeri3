package tech.ketc.numeri.domain

import tech.ketc.numeri.infra.AccountDatabase
import javax.inject.Inject

class AccountRepository @Inject constructor(private val db: AccountDatabase) : IAccountRepository {
    override fun getText(): String {
        db.accountDao()
        return "hoge"
    }
}