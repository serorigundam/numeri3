package net.ketc.numeri.domain.service

import net.ketc.numeri.TestInjectors
import net.ketc.numeri.domain.entity.*
import net.ketc.numeri.setLogStream
import net.ketc.numeri.util.ormlite.clearTable
import net.ketc.numeri.util.ormlite.createTable
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import twitter4j.Twitter
import twitter4j.auth.AccessToken
import javax.inject.Inject
import kotlin.test.assertEquals


@RunWith(RobolectricTestRunner::class)
class TweetsDisplayServiceTest {

    @Inject
    lateinit var tweetsDisplayService: TweetsDisplayService
    val client1 = createTwitterClientMock(1)
    val client2 = createTwitterClientMock(1)

    init {
        TestInjectors.testDomainComponent.inject(this)
    }

    private fun createTwitterClientMock(id: Long): TwitterClient {
        val mockClient = mock(TwitterClient::class.java)
        doReturn(id).`when`(mockClient).id
        val mockToken = mock(AccessToken::class.java)
        doReturn("hoge").`when`(mockToken).tokenSecret
        doReturn("hoge").`when`(mockToken).token
        doReturn(id).`when`(mockToken).userId
        val mockTwitter = mock(Twitter::class.java)
        doReturn(mockToken).`when`(mockTwitter).oAuthAccessToken
        doReturn(mockTwitter).`when`(mockClient).twitter

        mockClient.twitter.oAuthAccessToken

        return mockClient
    }

    @Before
    fun beforeEach() {
        setLogStream()
        createTable(TweetsDisplay::class, TweetsDisplayGroup::class)
    }

    @After
    fun afterEach() {
        tweetsDisplayService.getAllGroup().forEach {
            tweetsDisplayService.removeGroup(it)
        }
        clearTable(TweetsDisplay::class, TweetsDisplayGroup::class)
    }

    @Test
    fun createGroupTest() {
        tweetsDisplayService.createGroup()
        val groups = tweetsDisplayService.getAllGroup()
        assertEquals(1, groups.size)
        tweetsDisplayService.createGroup()
        val groups2 = tweetsDisplayService.getAllGroup()
        assertEquals(2, groups2.size)
    }

    @Test
    fun addToGroupTest() {
        val group = tweetsDisplayService.createGroup()
        tweetsDisplayService.createDisplay(group, client1, -1L, TweetsDisplayType.HOME, "")
        val displays = tweetsDisplayService.getDisplays(group)
        assertEquals(1, displays.size)
        assertEquals(0, displays[0].order)
        assertEquals(client1.id, displays[0].token.id)
        assertEquals(TweetsDisplayType.HOME, displays[0].type)
        tweetsDisplayService.createDisplay(group, client2, -1L, TweetsDisplayType.MENTIONS, "")
        val displays2 = tweetsDisplayService.getDisplays(group)
        assertEquals(2, displays2.size)
        assertEquals(1, displays2[1].order)
        assertEquals(client2.id, displays2[1].token.id)
        assertEquals(TweetsDisplayType.MENTIONS, displays2[1].type)
    }

    @Test
    fun deleteGroupTest() {
        val group = tweetsDisplayService.createGroup()
        tweetsDisplayService.createDisplay(group, client1, -1L, TweetsDisplayType.HOME, "")
        val groups = tweetsDisplayService.getAllGroup()
        assertEquals(1, groups.size)
        tweetsDisplayService.removeGroup(group)
        val groups1 = tweetsDisplayService.getAllGroup()
        assertEquals(0, groups1.size)
    }

    @Test(expected = GroupDoesNotExistWasSpecifiedException::class)
    fun addToGroupThrownGroupDoesNotExistWasSpecifiedExceptionTest1() {
        tweetsDisplayService.createDisplay(TweetsDisplayGroup(), client1, -1L, TweetsDisplayType.MENTIONS, "")
    }

    @Test(expected = GroupDoesNotExistWasSpecifiedException::class)
    fun addToGroupThrownGroupDoesNotExistWasSpecifiedExceptionTest2() {
        tweetsDisplayService.createDisplay(TweetsDisplayGroup(), client1, 1L, TweetsDisplayType.USER_LIST, "")
    }

    @Test(expected = GroupDoesNotExistWasSpecifiedException::class)
    fun removeGroupThrownGroupDoesNotExistWasSpecifiedException() {
        tweetsDisplayService.removeGroup(TweetsDisplayGroup())
    }
}