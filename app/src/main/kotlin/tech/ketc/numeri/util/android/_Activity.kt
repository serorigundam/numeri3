package tech.ketc.numeri.util.android

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View


inline fun <reified T : Activity> Activity.startActivityTransition(pair: Pair<String, View>) {
    val intent = Intent(this, T::class.java)
    startActivity(intent,
            ActivityOptions.makeSceneTransitionAnimation(this, pair.second, pair.first).toBundle())
}

fun AppCompatActivity.setUpSupportActionbar(toolbar: Toolbar) {
    setSupportActionBar(toolbar)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
}

val AppCompatActivity.supportActBar: ActionBar
    get() = supportActionBar ?: throw IllegalStateException()