package net.ketc.numeri.util.ormlite

import net.ketc.numeri.domain.entity.ClientToken
import net.ketc.numeri.domain.entity.createClientToken
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(JUnitPlatform::class)
class TransactionSpec : Spek({

    describe("transaction") {
        beforeEach {
            dropTable(ClientToken::class)
            createTable(ClientToken::class)
        }
        afterEach {
            dropTable(ClientToken::class)
        }
        it("create table and insert test") {
            transaction {
                val dao = dao(ClientToken::class)
                dao.create(createClientToken(0, "", ""))
                val size = dao.queryForAll().size
                assertEquals(size, 1)
            }
        }

    }

})