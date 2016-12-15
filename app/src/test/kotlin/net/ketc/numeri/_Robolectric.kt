package net.ketc.numeri

import android.app.Activity
import net.ketc.numeri.util.ormlite.DataBaseHelperFactory
import net.ketc.numeri.util.ormlite.getHelper
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowLog
import java.io.PrintStream

val context = Robolectric.buildActivity(Activity::class.java).get().applicationContext!!

fun setOnMemoryDB() {
    DataBaseHelperFactory.create = { getHelper(context) }
}

fun setLogStream(printStream: PrintStream = System.out) {
    ShadowLog.stream = printStream
}