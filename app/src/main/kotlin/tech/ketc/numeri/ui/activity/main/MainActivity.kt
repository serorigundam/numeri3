package tech.ketc.numeri.ui.activity.main

import android.arch.lifecycle.ViewModelProvider
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBarDrawerToggle
import android.util.ArrayMap
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import org.jetbrains.anko.*
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.infra.entity.TimelineGroup
import tech.ketc.numeri.ui.activity.setting.SettingsActivity
import tech.ketc.numeri.ui.components.AccountUIComponent
import tech.ketc.numeri.ui.fragment.dialog.MessageDialogFragment
import tech.ketc.numeri.ui.fragment.dialog.OnDialogItemSelectedListener
import tech.ketc.numeri.ui.fragment.main.MainFragment
import tech.ketc.numeri.ui.model.MainViewModel
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.android.fadeIn
import tech.ketc.numeri.util.android.fadeOut
import tech.ketc.numeri.util.arch.livedata.observeIfNonnullOnly
import tech.ketc.numeri.util.arch.owner.bindLaunch
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import tech.ketc.numeri.util.logTag
import java.io.Serializable
import javax.inject.Inject

class MainActivity : AppCompatActivity(), AutoInject,
        HasSupportFragmentInjector,
        NavigationView.OnNavigationItemSelectedListener,
        OnDialogItemSelectedListener,
        IMainUI by MainUI() {

    @Inject lateinit var mAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: MainViewModel by viewModel { mViewModelFactory }

    private val mDrawerToggle: ActionBarDrawerToggle by lazy { ActionBarDrawerToggle(this, drawer, 0, 0) }

    private var mNavigationState = NavigationState.MENU

    private val mGroupNameToViewId = ArrayMap<String, Int>()
    private var mShowingGroupName: String? = null
    private var mCurrentGroupList: List<TimelineGroup>? = null


    companion object {
        val INTENT_OAUTH = "INTENT_OAUTH"
        private val EXTRA_NAVIGATION_STATE = "EXTRA_NAVIGATION_STATE"
        private val EXTRA_SHOWING_GROUP_NAME = "EXTRA_SHOWING_GROUP_NAME"
        private val EXTRA_CURRENT_GROUP_LIST = "EXTRA_CURRENT_GROUP_LIST"
        private val EXTRA_GROUP_VIEW_ID = "EXTRA_GROUP_VIEW_ID"
        private val TAG_ADD_ACCOUNT_DIALOG = "TAG_ADD_ACCOUNT_DIALOG"
        private val REQUEST_CODE_ADD_ACCOUNT_DIALOG = 1010
    }

    private enum class NavigationState : Serializable {
        ACCOUNT, MENU
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = mAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        initializeUI()
        initializeUIBehavior()
        savedInstanceState?.let {
            restoreInstanceState(it)
        }
        initialize(savedInstanceState)
        observeTimelineChange()
    }

    private fun initializeUI() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        drawer.addDrawerListener(mDrawerToggle)
        mDrawerToggle.isDrawerIndicatorEnabled = true
        navigation.setNavigationItemSelectedListener(this)
    }

    private fun initializeUIBehavior() {
        navigationHeaderUI
                .toggleNavigationStateButton
                .setOnClickListener { toggleNavigationState() }
        accountListUI.addAccountButton.setOnClickListener {
            startAuthorization()
        }
    }

    private fun startAuthorization() {
        val addAccountButton = accountListUI.addAccountButton
        addAccountButton.isClickable = false
        mModel.createAuthorizationURL().observe(this) {
            it.ifPresent {
                val uri = Uri.parse(it)
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
            it.ifError {
                toast(getString(R.string.failed_generate_authentication_url))
                it.printStackTrace()
            }
            addAccountButton.isClickable = true
        }
    }

    private fun initialize(savedInstanceState: Bundle?) {
        mModel.clients.observe(this) { res ->
            res.ifPresent {
                if (it.isEmpty()) showAddAccountDialog()
                else initializeAccountListComponent(it)
                if (savedInstanceState == null)
                    initializeTimelineGroup()
            }
            res.ifError {
                toast(R.string.message_failed_user_info)
                it.printStackTrace()
            }
        }
    }

    private fun showAddAccountDialog() {
        val dialog = MessageDialogFragment
                .create(REQUEST_CODE_ADD_ACCOUNT_DIALOG, R.string.message_need_to_add_an_account)
        dialog.show(supportFragmentManager, TAG_ADD_ACCOUNT_DIALOG)
    }

    private fun initializeTimelineGroup() {
        mModel.groupList.observe(this) {
            it.ifPresent {
                if (it.isEmpty()) return@ifPresent
                mCurrentGroupList = it
                showFirstTimelineGroup(it)
            }
        }
    }

    private fun addAccountComponent(client: TwitterClient) {
        fun initializeAccountUIComponent(user: TwitterUser, component: AccountUIComponent) {
            component.userNameText.text = user.name
            component.screenNameText.text = user.screenName
            bindLaunch {
                val res = mModel.imageLoad(user.iconUrl).await()
                res.ifPresent { (b, _) -> component.iconImage.setImageBitmap(b) }
            }
        }

        fun observeAccountUpdate(user: TwitterUser, component: AccountUIComponent) {
            mModel.latestUpdatedUser.observeIfNonnullOnly(this, { it.id == user.id }) { updatedUser ->
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

        mModel.getClientUser(this, client) {
            it.ifPresent { addAccountComponent(it) }
            it.ifError {
                val message = getString(R.string.message_failed_user_info)
                toast("$message accountId:${client.id}")
            }
        }
    }

    private fun initializeAccountListComponent(clients: Set<TwitterClient>) {
        clients.forEach { addAccountComponent(it) }
    }

    private fun addTimelineGroupView(groupName: String): View {
        var viewId = mGroupNameToViewId[groupName]
        var view: View? = if (viewId != null) findViewById(viewId) else null
        if (view != null) return view.also { Logger.v(logTag, "addTimelineGroupView() created:$groupName:$viewId") }

        if (viewId == null) viewId = View.generateViewId()
        mGroupNameToViewId.put(groupName, viewId)
        Logger.v(javaClass.name, "addTimelineGroupView new")
        view = ctx.frameLayout {
            id = viewId!!
            lparams(matchParent, matchParent)
            tag = groupName
            visibility = View.GONE
        }
        columnGroupWrapper.addView(view)
        Logger.v(logTag, "addTimelineGroupView() create:$groupName:$viewId")
        return view
    }

    private fun showTimelineGroup(groupName: String) {
        supportActionBar!!.subtitle = groupName
        val view = addTimelineGroupView(groupName)
        val fragment = supportFragmentManager.findFragmentByTag(groupName)
        val viewId = view.id
        fragment ?: supportFragmentManager.beginTransaction()
                .add(viewId, MainFragment.create(groupName), groupName)
                .commit()
        Logger.v(javaClass.name, "showTimelineGroup($groupName) fragment exists ${fragment != null}")
        if (mShowingGroupName != null) {
            val id = mGroupNameToViewId[groupName]
            id?.let { findViewById<View>(it).visibility = View.GONE }
        }
        view.visibility = View.VISIBLE
        mShowingGroupName = groupName
    }

    private fun removeTimelineGroup(groupName: String, groupList: List<TimelineGroup>) {
        val viewId = mGroupNameToViewId[groupName]
        viewId ?: return
        val fragment = fragmentManager.findFragmentById(viewId)
        if (fragment != null) {
            Logger.v(javaClass.name, "remove$groupName")
            fragmentManager.beginTransaction().remove(fragment).commit()
            columnGroupWrapper.removeView(findViewById(viewId))
            if (mShowingGroupName == groupName) showFirstTimelineGroup(groupList)
        }
    }

    private fun showFirstTimelineGroup(groupList: List<TimelineGroup>) {
        Logger.v(javaClass.name, "showFirstTimelineGroup")
        val group = groupList.firstOrNull()
        if (group != null) showTimelineGroup(group.name)
        else supportActionBar!!.subtitle = ""
    }

    private fun observeTimelineChange() {
        fun apply(newGroupList: List<TimelineGroup>) {
            val previousList = mCurrentGroupList
            if (previousList == null) {
                showFirstTimelineGroup(newGroupList)
            } else {
                val deleted = previousList.filter { group -> !newGroupList.any { group.name == it.name } }
                deleted.forEach { removeTimelineGroup(it.name, newGroupList) }
            }
            mCurrentGroupList = newGroupList
        }
        mModel.timelineChange(this) {
            Logger.v(javaClass.name, "timelineChange")
            mModel.groupList.observe(this) {
                it.ifPresent { apply(it) }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val oauthIntent = intent.getParcelableExtra<Intent>(INTENT_OAUTH) ?: return
        mModel.onNewIntent(oauthIntent, this) {
            it.ifPresent { addAccountComponent(it) }
            it.ifError {
                toast(R.string.authentication_failure)
                it.printStackTrace()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(EXTRA_NAVIGATION_STATE, mNavigationState)
        mShowingGroupName?.let { outState.putString(EXTRA_SHOWING_GROUP_NAME, mShowingGroupName) }
        mCurrentGroupList?.let {
            outState.putSerializable(EXTRA_CURRENT_GROUP_LIST, it.toTypedArray())
            it.forEach { group ->
                val name = group.name
                outState.putInt(EXTRA_GROUP_VIEW_ID + name, mGroupNameToViewId[name]!!)
            }
        }
        super.onSaveInstanceState(outState)
    }

    private fun restoreInstanceState(savedInstanceState: Bundle) {
        Logger.v(logTag, "restoreSaveInstance")
        mNavigationState = savedInstanceState.getSerializable(EXTRA_NAVIGATION_STATE) as NavigationState
        mShowingGroupName = savedInstanceState.getString(EXTRA_SHOWING_GROUP_NAME)
        val previousGroupArray = savedInstanceState.getSerializable(EXTRA_CURRENT_GROUP_LIST) as? Array<*>
        previousGroupArray?.let { groupArray ->
            Logger.v(logTag, "restore timelineGroup")
            val groupList = ArrayList<TimelineGroup>()
            groupArray.mapTo(groupList) { it as TimelineGroup }
            mCurrentGroupList = groupList

            val nameToIdMap = groupList
                    .map { it.name to savedInstanceState.getInt(EXTRA_GROUP_VIEW_ID + it.name) }
                    .toMap()
            mGroupNameToViewId.putAll(nameToIdMap)
            groupList.forEach {
                addTimelineGroupView(it.name)
            }
            mShowingGroupName?.let { showTimelineGroup(it) }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mDrawerToggle.syncState()
    }

    override fun onResume() {
        super.onResume()
        toggleNavigationState(mNavigationState)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mDrawerToggle.onOptionsItemSelected(item))
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
        if (state != null) mNavigationState = when (state) {
            NavigationState.MENU -> NavigationState.ACCOUNT
            NavigationState.ACCOUNT -> NavigationState.MENU
        }
        val drawable: Drawable
        when (mNavigationState) {
            NavigationState.MENU -> {
                mNavigationState = NavigationState.ACCOUNT
                navigation.menu.setGroupVisible(R.id.main_menu, false)
                drawable = getDrawable(R.drawable.ic_expand_less_white_24dp)
                navigationHeaderUI.navigationStateIndicator.image = drawable
                accountListUI.container.animate().fadeIn().withEndAction {
                    accountListUI.container.visibility = View.VISIBLE
                }.start()
            }
            NavigationState.ACCOUNT -> {
                drawable = getDrawable(R.drawable.ic_expand_more_white_24dp)
                mNavigationState = NavigationState.MENU
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
            R.id.setting -> startActivity<SettingsActivity>()
            else -> return super.onOptionsItemSelected(item)
        }
        drawer.closeDrawer(navigation)
        return true
    }

    override fun onDialogItemSelected(requestCode: Int, itemId: Int) {
        fun accountDialog() {
            when (itemId) {
                R.string.yes -> startAuthorization()
                R.string.cancel -> showAddAccountDialog()
            }
        }
        when (requestCode) {
            REQUEST_CODE_ADD_ACCOUNT_DIALOG -> accountDialog()
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