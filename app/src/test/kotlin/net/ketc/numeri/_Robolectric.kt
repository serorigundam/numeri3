package net.ketc.numeri

import android.app.Activity
import net.ketc.numeri.util.ormlite.DataBaseHelperFactory
import net.ketc.numeri.util.ormlite.getHelper
import org.robolectric.Robolectric

val context = Robolectric.buildActivity(Activity::class.java).get().applicationContext!!

fun setOnMemoryDB() {
    DataBaseHelperFactory.create = { getHelper(context) }
}