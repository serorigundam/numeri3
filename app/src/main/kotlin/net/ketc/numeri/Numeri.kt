package net.ketc.numeri

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import net.ketc.numeri.domain.android.service.TweetService
import net.ketc.numeri.domain.entity.entities
import net.ketc.numeri.util.ormlite.createTable


class Numeri : Application() {
    override fun onCreate() {
        super.onCreate()
        Injectors.test = false
        cApplication = this
        createTable(*entities)
        Fabric.with(this, Crashlytics())
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val name = applicationContext.getString(R.string.channel_send_tweet)
            val sendTweetNotificationChannel = NotificationChannel(TweetService.CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
            sendTweetNotificationChannel.enableLights(true)
            sendTweetNotificationChannel.lightColor = getColor(R.color.colorAccent)
            notificationManager.createNotificationChannel(sendTweetNotificationChannel)
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var cApplication: Application? = null
        val application: Application
            get() = cApplication!!
    }
}
