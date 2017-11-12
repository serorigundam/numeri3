package tech.ketc.numeri.ui.activity.main

import android.arch.lifecycle.ViewModelProvider
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v7.app.ActionBarDrawerToggle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import org.jetbrains.anko.image
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.ui.components.AccountUIComponent
import tech.ketc.numeri.ui.model.MainViewModel
import tech.ketc.numeri.util.android.fadeIn
import tech.ketc.numeri.util.android.fadeOut
import tech.ketc.numeri.util.arch.livedata.observeIfNonnullOnly
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import java.io.Serializable
import javax.inject.Inject

class MainActivity : AppCompatActivity(), AutoInject, NavigationView.OnNavigationItemSelectedListener, IMainUI by MainUI() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: MainViewModel by viewModel { viewModelFactory }

    private val drawerToggle: ActionBarDrawerToggle by lazy { ActionBarDrawerToggle(this, drawer, 0, 0) }

    private var navigationState = NavigationState.MENU

    companion object {
        val INTENT_OAUTH = "INTENT_OAUTH"
        private val EXTRA_NAVIGATION_STATE = "EXTRA_NAVIGATION_STATE"
    }

    private enum class NavigationState : Serializable {
        ACCOUNT, MENU
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        initializeUI()
        initializeUIBehavior()
        initialize()
    }

    private fun initializeUI() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        drawer.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        navigation.setNavigationItemSelectedListener(this)
    }

    private fun initializeUIBehavior() {
        navigationHeaderUI
                .toggleNavigationStateButton
                .setOnClickListener { toggleNavigationState() }
        accountListUI.addAccountButton.setOnClickListener { v ->
            v.isClickable = false
            model.createAuthorizationURL().observe(this) {
                it.ifPresent {
                    val uri = Uri.parse(it)
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
                it.ifError {
                    toast(getString(R.string.failed_generate_authentication_url))
                    it.printStackTrace()
                }
                v.isClickable = true
            }
        }
    }

    private fun initialize() {
        model.clients.observe(this) { res ->
            res.ifPresent {
                initializeAccountListComponent(it)
            }
            res.ifError {
                toast(R.string.message_failed_user_info)
                it.printStackTrace()
            }
        }
    }


    private fun initializeAccountUIComponent(user: TwitterUser, component: AccountUIComponent) {
        component.userNameText.text = user.name
        component.screenNameText.text = user.screenName
        model.imageLoad(this, user.iconUrl) {
            it.ifPresent { (bitmap, _) ->
                component.iconImage.setImageBitmap(bitmap)
            }
            it.ifError { it.printStackTrace() }
        }
    }

    private fun initializeAccountListComponent(clients: Set<TwitterClient>) {
        fun observeAccountUpdate(user: TwitterUser, component: AccountUIComponent) {
            model.latestUpdatedUser.observeIfNonnullOnly(this, { it.id == user.id }) { updatedUser ->
                initializeAccountUIComponent(updatedUser, component)
            }
        }

        fun addAccountComponent(user: TwitterUser) {
            val component = AccountUIComponent()
            val view = component.createView(this)
            initializeAccountUIComponent(user, component)
            accountListUI.accountList.addView(view)
            observeAccountUpdate(user, component)
        }
        clients.forEach { client ->
            model.getClientUser(this, client) { res ->
                res.ifPresent { addAccountComponent(it) }
                res.ifError {
                    val message = getString(R.string.message_failed_user_info)
                    toast("$message id:${client.id}")
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val oauthIntent = intent.getParcelableExtra<Intent>(INTENT_OAUTH) ?: return
        model.onNewIntent(oauthIntent, this) {
            it.ifPresent {
                model.getClientUser(this, it) {
                    it.ifPresent {
                        val component = AccountUIComponent()
                        initializeAccountUIComponent(it, component)
                        accountListUI.accountList.addView(component.createView(this))
                    }
                }
            }
            it.ifError { it.printStackTrace() }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(EXTRA_NAVIGATION_STATE, navigationState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        navigationState = savedInstanceState.getSerializable(EXTRA_NAVIGATION_STATE) as NavigationState
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        toggleNavigationState(navigationState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (drawer.isDrawerOpen(navigation)) {
                    drawer.closeDrawer(navigation)
                } else {
                    moveTaskToBack(true)
                }
            }
            else -> return super.onKeyDown(keyCode, event)
        }
        return true
    }

    private fun toggleNavigationState(state: NavigationState? = null) {
        if (state != null) navigationState = when (state) {
            NavigationState.MENU -> NavigationState.ACCOUNT
            NavigationState.ACCOUNT -> NavigationState.MENU
        }
        val drawable: Drawable
        when (navigationState) {
            NavigationState.MENU -> {
                navigationState = NavigationState.ACCOUNT
                navigation.menu.setGroupVisible(R.id.main_menu, false)
                drawable = getDrawable(R.drawable.ic_expand_less_white_24dp)
                navigationHeaderUI.navigationStateIndicator.image = drawable
                accountListUI.container.animate().fadeIn().withEndAction {
                    accountListUI.container.visibility = View.VISIBLE
                }.start()
            }
            NavigationState.ACCOUNT -> {
                drawable = getDrawable(R.drawable.ic_expand_more_white_24dp)
                navigationState = NavigationState.MENU
                navigationHeaderUI.navigationStateIndicator.image = drawable
                accountListUI.container.visibility = View.GONE
                accountListUI.container.animate().fadeOut().withEndAction {
                    navigation.menu.setGroupVisible(R.id.main_menu, true)
                }.start()
            }
        }
    }

    //interface impl
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.column_manage -> toast("not implement")//todo not implement
            R.id.changing_column_group -> toast("not implement")//todo not implement
            else -> return super.onOptionsItemSelected(item)
        }
        drawer.closeDrawer(navigation)
        return true
    }


    class OauthActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            startActivity<MainActivity>(MainActivity.INTENT_OAUTH to intent)
            finish()
        }
    }
}