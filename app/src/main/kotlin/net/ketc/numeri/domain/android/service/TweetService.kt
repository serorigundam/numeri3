package net.ketc.numeri.domain.android.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import net.ketc.numeri.R
import net.ketc.numeri.domain.model.TwitterUser
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.view.activity.TweetActivity
import net.ketc.numeri.util.log.i
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.rx.AutoDisposableImpl
import net.ketc.numeri.util.rx.MySchedulers
import net.ketc.numeri.util.twitter.sendTweet
import java.io.File

class TweetService : Service(), ITweetService, AutoDisposable by AutoDisposableImpl() {

    private val binder = ITweetService.Binder(this)

    override fun onBind(intent: Intent?) = binder

    private var count = 0
    private var taskCount = 0
    private var sending = false
    private val notificationId = 200

    override fun sendTweet(client: TwitterClient, clientUser: TwitterUser, text: String,
                           inReplyToStatusId: Long?,
                           mediaList: List<File>?,
                           isPossiblySensitive: Boolean) {

        fun createNotification(subText: String): Notification {
            return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
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
                    .setSmallIcon(R.mipmap.ic_launcher)
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

        singleTask(MySchedulers.twitter) {
            client.sendTweet(text, inReplyToStatusId, mediaList, isPossiblySensitive)
        } error {
            finish(getString(R.string.send_failure))
            it.printStackTrace()
        } success {
            finish(getString(R.string.send_success))
        }

        if (count == 100) {
            count = 0
        } else {
            count++
        }
    }

    override fun onCreate() {
        super.onCreate()
        i(TAG, "onCreate")
    }

    override fun onDestroy() {
        dispose()
        i(TAG, "onDestroy")
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
