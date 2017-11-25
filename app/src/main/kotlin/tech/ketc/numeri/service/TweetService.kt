package tech.ketc.numeri.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.ui.activity.tweet.TweetActivity
import tech.ketc.numeri.util.arch.response.orError
import tech.ketc.numeri.util.arch.response.response
import tech.ketc.numeri.util.coroutine.dispose
import tech.ketc.numeri.util.twitter4j.sendTweet
import java.io.File

class TweetService : Service(), ITweetService {

    private val binder = ITweetService.Binder(this)

    override fun onBind(intent: Intent?) = binder

    private var count = 0
    private var taskCount = 0
    private var sending = false
    private val notificationId = 200

    private val jobList = ArrayList<Job>()

    override fun sendTweet(client: TwitterClient, clientUser: TwitterUser, text: String,
                           inReplyToStatusId: Long?,
                           mediaList: List<File>?,
                           isPossiblySensitive: Boolean) {

        fun createNotification(subText: String): Notification {
            return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentText("${clientUser.screenName} : $text")
                    .setSubText(subText)
                    .setColor(getColor(R.color.colorPrimary))
                    .build()
        }

        if (!sending) {
            val intents = Intent(applicationContext, TweetActivity::class.java)
            val contentIntent = PendingIntent.getActivity(applicationContext, 0, intents,
                    PendingIntent.FLAG_UPDATE_CURRENT)

            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                    .setContentText(getString(R.string.tweet_sending))
                    .setSubText("send tweet")
                    .setColor(getColor(R.color.colorPrimary))
                    .build()
            notification.contentIntent = contentIntent
            startForeground(notificationId, notification)
            sending = true
        }
        val currentCount = count
        taskCount++

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(TAG, currentCount, createNotification(getString(R.string.sending)))

        fun finish(text: String) {
            taskCount--
            if (taskCount == 0) {
                stopForeground(true)
                sending = false
            }
            notificationManager.notify(TAG, currentCount, createNotification(text))
        }

        val job = launch {
            async {
                response {
                    client.sendTweet(text, inReplyToStatusId, mediaList, isPossiblySensitive)
                }
            }.await().orError {
                finish(getString(R.string.send_failure))
            } ?: return@launch
            finish(getString(R.string.send_success))
        }
        job.invokeOnCompletion { jobList.remove(job) }
        jobList.add(job)

        if (count == 100) {
            count = 0
        } else {
            count++
        }
    }

    override fun onDestroy() {
        jobList.forEach(Job::dispose)
        super.onDestroy()
    }

    companion object {
        val TAG = "tech.ketc.numeri3:TweetService"
        val CHANNEL_ID = "tech.ketc.numeri3:TweetService"
    }
}

interface ITweetService {
    fun sendTweet(client: TwitterClient, clientUser: TwitterUser, text: String = "",
                  inReplyToStatusId: Long? = null,
                  mediaList: List<File>? = null,
                  isPossiblySensitive: Boolean = false)

    class Binder(private val tweetService: ITweetService) : android.os.Binder() {
        fun getService(): ITweetService {
            return tweetService
        }
    }
}