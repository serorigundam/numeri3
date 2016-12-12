package net.ketc.numeri.domain.service

import net.ketc.numeri.domain.entity.ClientToken
import net.ketc.numeri.domain.inject
import net.ketc.numeri.util.ormlite.createTable
import net.ketc.numeri.util.ormlite.dropTable
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.test.assertEquals

@RunWith(JUnitPlatform::class)
class OAuthServiceSpec : Spek({
    describe("oauthService") {
        beforeEach {
            createTable(ClientToken::class)
        }
        afterEach {
            oAuthService.clients().toTypedArray().forEach {
                oAuthService.deleteClient(it)
            }
            dropTable(ClientToken::class)
        }
        it("create client") {
            oAuthService.createAuthorizationURL()
            val client = oAuthService.createTwitterClient("oauthVerifier")
            val clients = oAuthService.clients()
            assertEquals(1, clients.size)
            assertEquals(client.id, clients[0].id)
            oAuthService.createAuthorizationURL()
            val client1 = oAuthService.createTwitterClient("oauthVerifier")
            val clients1 = oAuthService.clients()
            assertEquals(2, clients1.size)
            assertEquals(client1.id, clients1[1].id)
        }

        it("hoge") {
            oAuthService.createAuthorizationURL()
            val client = oAuthService.createTwitterClient("oauthVerifier")
            val clients = oAuthService.clients()
            assertEquals(1, clients.size)
            assertEquals(client.id, clients[0].id)
            oAuthService.deleteClient(client)
            assertEquals(0, oAuthService.clients().size)
        }

    }
})

private val oAuthService: OAuthService
    get() = OAuthServiceSpecDependency().oAuthService

class OAuthServiceSpecDependency() {
    @Inject
    lateinit var oAuthService: OAuthService

    init {
        inject()
    }
}