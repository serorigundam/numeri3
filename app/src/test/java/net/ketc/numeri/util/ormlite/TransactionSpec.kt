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
        it("create table and insert test") {
            createTable(ClientToken::class)
            transaction {
                val dao = dao(ClientToken::class)
                dao.create(createClientToken(0, "", ""))
                val size = dao.queryForAll().size
                assertEquals(size, 1)
            }
        }

        afterEach {
            dropTable(ClientToken::class)
        }
    }

})