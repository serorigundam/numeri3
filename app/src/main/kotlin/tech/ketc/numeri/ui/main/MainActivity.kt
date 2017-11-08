package tech.ketc.numeri.ui.main

import android.arch.lifecycle.ViewModelProvider
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import org.jetbrains.anko.image
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import tech.ketc.numeri.R
import tech.ketc.numeri.ui.components.AccountUIComponent
import tech.ketc.numeri.ui.model.MainViewModel
import tech.ketc.numeri.util.android.fadeIn
import tech.ketc.numeri.util.android.fadeOut
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import java.io.Serializable
import javax.inject.Inject

class MainActivity : AppCompatActivity(), AutoInject, IMainUI by MainUI() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: MainViewModel by viewModel { viewModelFactory }
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
        initialize()
    }

    private fun initializeUI() {
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
        model.clients.observe(this) {
            it.ifPresent {
                //todo 仮置き
                it.map { it.id.toString() }.forEach {
                    toast(it)
                    val component = AccountUIComponent()
                    accountListUI.accountList.addView(component.createView(this))
                    component.screenNameText.text = it
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val oauthIntent = intent.getParcelableExtra<Intent>(INTENT_OAUTH) ?: return
        model.onNewIntent(oauthIntent, this) {
            it.ifPresent {
                val idStr = it.id.toString()
                toast(idStr)
                val component = AccountUIComponent()
                accountListUI.accountList.addView(component.createView(this))
                component.screenNameText.text = idStr
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

    class OauthActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            startActivity<MainActivity>(MainActivity.INTENT_OAUTH to intent)
            finish()
        }
    }
}