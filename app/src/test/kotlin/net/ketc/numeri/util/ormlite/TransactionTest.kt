package net.ketc.numeri.util.ormlite

import net.ketc.numeri.domain.entity.ClientToken
import net.ketc.numeri.domain.entity.createClientToken
import net.ketc.numeri.setOnMemoryDB
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class TransactionTest {
    @Before
    fun beforeEach() {
        setOnMemoryDB()
        createTable(ClientToken::class)
    }

    @After
    fun afterEach() {
        clearTable(ClientToken::class)
    }

    @Test
    fun transactionTest() = transaction {
        val dao = dao(ClientToken::class)
        dao.create(createClientToken(1L, "hoge", "hoge"))
        assertEquals(1, dao.queryForAll().size)
    }
}