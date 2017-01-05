package net.ketc.numeri.domain.service

import android.test.mock.MockContext
import net.ketc.numeri.TestInjectors
import net.ketc.numeri.domain.entity.*
import net.ketc.numeri.setLogStream
import net.ketc.numeri.setOnMemoryDB
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
        createTable(TweetsDisplay::class, TimeLineDisplay::class, TweetsDisplayGroup::class)
    }

    @After
    fun afterEach() {
        tweetsDisplayService.getAllDisplays()
                .flatMap { it }
                .map { it.group.id }
                .distinct()
                .forEach {
                    tweetsDisplayService.removeGroup(it)
                }
        clearTable(TweetsDisplay::class, TimeLineDisplay::class, TweetsDisplayGroup::class)
    }

    @Test
    fun createGroupTest() {
        tweetsDisplayService.createGroup(client1, TimeLineType.HOME)
        tweetsDisplayService.createGroup(client1, 1, TweetsDisplayType.USER_LIST)
        val allDisplays = tweetsDisplayService.getAllDisplays()
        assertEquals(2, allDisplays.size)
        assertEquals(1, allDisplays[0].size)
        assertEquals(1, allDisplays[1].size)
    }

    @Test
    fun addToGroupTest() {
        tweetsDisplayService.createGroup(client1, TimeLineType.HOME)
        tweetsDisplayService.createGroup(client1, 1, TweetsDisplayType.USER_LIST)
        val allDisplays = tweetsDisplayService.getAllDisplays()
        allDisplays.forEachIndexed { i, list ->
            tweetsDisplayService.addToGroup(list.first().group, client1, TimeLineType.MENTIONS)
            tweetsDisplayService.addToGroup(list.first().group, client1, i.toLong(), TweetsDisplayType.PUBLIC)
        }

        tweetsDisplayService.getAllDisplays().forEach {
            assertEquals(3, it.size)
        }
    }

    @Test
    fun deleteGroupTest() {
        tweetsDisplayService.createGroup(client1, TimeLineType.HOME)
        tweetsDisplayService.createGroup(client1, 1, TweetsDisplayType.USER_LIST)

        val displays = tweetsDisplayService.getAllDisplays()
        val size = displays.size
        assertEquals(2, size)
        val id1 = displays[0].first().group.id
        val id2 = displays[1].first().group.id
        tweetsDisplayService.removeGroup(id1)
        assertEquals(1, tweetsDisplayService.getAllDisplays().size)
        tweetsDisplayService.removeGroup(id2)
        assertEquals(0, tweetsDisplayService.getAllDisplays().size)
    }

    @Test(expected = GroupDoesNotExistWasSpecifiedException::class)
    fun addToGroupThrownGroupDoesNotExistWasSpecifiedExceptionTest1() {
        tweetsDisplayService.addToGroup(TweetsDisplayGroup(), client1, TimeLineType.MENTIONS)
    }

    @Test(expected = GroupDoesNotExistWasSpecifiedException::class)
    fun addToGroupThrownGroupDoesNotExistWasSpecifiedExceptionTest2() {
        tweetsDisplayService.addToGroup(TweetsDisplayGroup(), client1, 1L, TweetsDisplayType.USER_LIST)
    }

    @Test(expected = GroupDoesNotExistWasSpecifiedException::class)
    fun removeGroupThrownGroupDoesNotExistWasSpecifiedException() {
        tweetsDisplayService.removeGroup(1)
    }
}