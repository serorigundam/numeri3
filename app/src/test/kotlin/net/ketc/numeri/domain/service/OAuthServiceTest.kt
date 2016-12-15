package net.ketc.numeri.domain.service

import net.ketc.numeri.*
import net.ketc.numeri.domain.entity.ClientToken
import net.ketc.numeri.util.ormlite.createTable
import net.ketc.numeri.util.ormlite.clearTable
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import javax.inject.Inject
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class OAuthServiceTest {
    @Inject
    lateinit var oAuthService: OAuthService

    init {
        TestInjectors.testDomainComponent.inject(this)
    }

    @Before
    fun beforeEach() {
        setLogStream()
        setOnMemoryDB()
        createTable(ClientToken::class)
    }

    @After
    fun afterEach() {
        oAuthService.clients().toTypedArray().forEach {
            oAuthService.deleteClient(it)
        }
        clearTable(ClientToken::class)
    }

    @Test
    fun createClientTest() {
        printNanoTime("create client time") {
            oAuthService.createAuthorizationURL()
            val client = oAuthService.createTwitterClient("1")
            val clients = oAuthService.clients()
            assertEquals(1, clients.size)
            assertEquals(client.id, clients[0].id)
        }
    }

    @Test
    fun deleteClientTest() {
        oAuthService.createAuthorizationURL()
        val client = oAuthService.createTwitterClient("1")
        val clients = oAuthService.clients()
        assertEquals(1, clients.size)
        oAuthService.deleteClient(client)
        assertEquals(0, oAuthService.clients().size)
    }

    @Test(expected = TriedSaveSavedUserTokenException::class)
    fun thrownTriedSaveSavedUserTokenExceptionTest() {
        oAuthService.createAuthorizationURL()
        oAuthService.createTwitterClient("1")
        oAuthService.createAuthorizationURL()
        oAuthService.createTwitterClient("1")
    }

    @Test(expected = AuthenticationNotStartedException::class)
    fun thrownAuthenticationNotStartedExceptionTest() {
        oAuthService.createTwitterClient("1")
    }
}
