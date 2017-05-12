package net.ketc.numeri.domain.android.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.NotificationCompat
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

class TweetService : Service(), AutoDisposable by AutoDisposableImpl() {

    private val binder = Binder(this)

    override fun onBind(intent: Intent?) = binder

    private var count = 0
    private var taskCount = 0
    private var sending = false
    private val notificationId = 200

    fun sendTweet(client: TwitterClient, clientUser: TwitterUser, text: String = "",
                  inReplyToStatusId: Long? = null,
                  mediaList: List<File>? = null,
                  isPossiblySensitive: Boolean = false) {

        fun createNotification(subText: String): Notification {
            return NotificationCompat.Builder(applicationContext)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                    .setContentText("${clientUser.screenName} : $text")
                    .setSubText(subText)
                    .build()
        }

        if (!sending) {
            val intents = Intent(applicationContext, TweetActivity::class.java)
            val contentIntent = PendingIntent.getActivity(applicationContext, 0, intents,
                    Intent.FLAG_ACTIVITY_NEW_TASK)
            val notification = NotificationCompat.Builder(applicationContext)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                    .setContentText(getString(R.string.tweet_sending))
                    .setSubText("tweet sender")
                    .build()
            notification.contentIntent = contentIntent
            startForeground(notificationId, notification)
            sending = true
        }
        val currentCount = count
        taskCount++
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        fun finish(text: String) {
            taskCount--
            if (taskCount == 0) {
                stopForeground(true)
                sending = false
            }
            notificationManager.notify(TAG, currentCount, createNotification(text))
        }
        notificationManager.notify(TAG, currentCount, createNotification(getString(R.string.sending)))
        singleTask(MySchedulers.twitter) {
            client.sendTweet(text, inReplyToStatusId, mediaList, isPossiblySensitive)
        } error {
            finish(getString(R.string.send_failure))
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
        val TAG = "TweetService"
    }

    class Binder(private val tweetService: TweetService) : android.os.Binder() {
        fun getService(): TweetService {
            return tweetService
        }
    }
}