package tech.ketc.numeri.ui.activity.main

import android.app.Dialog
import android.arch.lifecycle.ViewModelProvider
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
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
import org.jetbrains.anko.support.v4.ctx
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.infra.entity.TimelineGroup
import tech.ketc.numeri.ui.activity.setting.SettingsActivity
import tech.ketc.numeri.ui.activity.timelinemanage.TimelineManageActivity
import tech.ketc.numeri.ui.activity.tweet.TweetActivity
import tech.ketc.numeri.ui.components.AccountUIComponent
import tech.ketc.numeri.ui.components.createBottomSheetUIComponent
import tech.ketc.numeri.ui.components.createMenuItemUIComponent
import tech.ketc.numeri.ui.fragment.dialog.MessageDialogFragment
import tech.ketc.numeri.ui.fragment.dialog.OnDialogItemSelectedListener
import tech.ketc.numeri.ui.fragment.main.MainFragment
import tech.ketc.numeri.ui.model.MainViewModel
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.android.*
import tech.ketc.numeri.util.android.ui.gesture.SimpleDoubleClickHelper
import tech.ketc.numeri.util.arch.livedata.observeIfNonnullOnly
import tech.ketc.numeri.util.arch.owner.bindLaunch
import tech.ketc.numeri.util.arch.response.orError
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.copy
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
    private var mCurrentGroupList: MutableList<TimelineGroup> = ArrayList()
    private var mInitialized = false
    private var mIsFabMenuShowing = false


    companion object {
        val INTENT_OAUTH = "INTENT_OAUTH"
        private val EXTRA_NAVIGATION_STATE = "EXTRA_NAVIGATION_STATE"
        private val EXTRA_SHOWING_GROUP_NAME = "EXTRA_SHOWING_GROUP_NAME"
        private val EXTRA_CURRENT_GROUP_LIST = "EXTRA_CURRENT_GROUP_LIST"
        private val EXTRA_GROUP_VIEW_ID = "EXTRA_GROUP_VIEW_ID"
        private val EXTRA_IS_FAB_MENU_SHOWING = "EXTRA_IS_FAB_MENU_SHOWING"
        private val TAG_ADD_ACCOUNT_DIALOG = "TAG_ADD_ACCOUNT_DIALOG"
        private val TAG_TIMELINE_SELECT_DIALOG = "TAG_TIMELINE_SELECT_DIALOG"
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
        initialize()
        observeTimelineChange()
    }

    private fun initializeUI() {
        setUpSupportActionbar(toolbar)
        drawer.addDrawerListener(mDrawerToggle)
        mDrawerToggle.isDrawerIndicatorEnabled = true
        navigation.setNavigationItemSelectedListener(this)
    }

    private fun initializeUIBehavior() {
        navigationHeaderUI
                .toggleNavigationStateButton
                .setOnClickListener { toggleNavigationState() }
        accountListUI
                .addAccountButton
                .setOnClickListener { startAuthorization() }
        SimpleDoubleClickHelper(onDoubleClick = { onDoubleClickTweetFab() },
                onClick = { onClickTweetFab() }).attachTo(tweetFab)
        groupChangeFab.setOnClickListener { onClickGroupChangeFab() }
    }

    private fun startAuthorization() {
        bindLaunch {
            accountListUI.addAccountButton.isClickable = false
            val res = mModel.createAuthorizationURL().await()
            val url = res.orError {
                accountListUI.addAccountButton.isClickable = true
                toast(getString(R.string.failed_generate_authentication_url))
            } ?: return@bindLaunch
            val uri = Uri.parse(url)
            startActivity(Intent(Intent.ACTION_VIEW, uri))
            accountListUI.addAccountButton.isClickable = true
        }
    }

    private fun initialize() {
        bindLaunch {
            val clientsRes = mModel.clients().await()
            val clients = clientsRes.orError {
                toast(R.string.message_failed_user_info)
            } ?: return@bindLaunch
            mInitialized = clients.isEmpty()
            if (mInitialized) showAddAccountDialog()
            else initializeAccountListComponent(clients)
            initializeTimelineGroup()
            mInitialized = true
        }
    }

    private fun showAddAccountDialog() {
        val dialog = MessageDialogFragment
                .create(REQUEST_CODE_ADD_ACCOUNT_DIALOG,
                        getString(R.string.message_need_to_add_an_account),
                        positiveId = R.string.add)
        dialog.show(supportFragmentManager, TAG_ADD_ACCOUNT_DIALOG)
    }

    private fun initializeTimelineGroup() {
        Logger.v(logTag, "initializeTimelineGroup")
        bindLaunch {
            val groupList = mModel.loadGroupListBlocking().await().result
            apply(groupList)
        }
    }

    private fun addAccountComponent(client: TwitterClient) {
        fun initializeAccountUIComponent(user: TwitterUser, component: AccountUIComponent) {
            component.userNameText.text = user.name
            component.screenNameText.text = user.screenName
            bindLaunch {
                val res = mModel.loadImage(user.iconUrl).await()
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

        bindLaunch {
            val res = mModel.getClientUser(client).await()
            val user = res.orError {
                val message = getString(R.string.message_failed_user_info)
                toast("$message accountId:${client.id}")
            } ?: return@bindLaunch
            addAccountComponent(user)
        }
    }

    private fun initializeAccountListComponent(clients: Set<TwitterClient>) {
        clients.forEach { addAccountComponent(it) }
    }

    private fun createOrGetTimelineGroupView(groupName: String): View {
        val viewId = mGroupNameToViewId.getOrPut(groupName) { View.generateViewId() }
        return columnGroupWrapper.findViewById(viewId) ?: ctx.relativeLayout {
            Logger.v(this@MainActivity.logTag, "createOrGetTimelineGroupView create view[$viewId] $groupName ")
            lparams(matchParent, matchParent)
            id = viewId
        }.also { columnGroupWrapper.addView(it) }
    }

    private fun showTimelineGroup(groupName: String) {
        supportActBar.subtitle = groupName
        if (mShowingGroupName == groupName) return
        val view = createOrGetTimelineGroupView(groupName)
        val viewId = view.id
        val fragment = supportFragmentManager.findFragmentById(viewId)
                ?: MainFragment.create(groupName).also {
            supportFragmentManager.beginTransaction()
                    .add(viewId, it, groupName)
                    .commit()
            Logger.v(javaClass.name, "showTimelineGroup($groupName)  generate fragment")
        }
        if (mShowingGroupName != null) {
            Logger.v(logTag, "showTimelineGroup hide $mShowingGroupName")
            val v = createOrGetTimelineGroupView(mShowingGroupName!!)
            supportFragmentManager.findFragmentById(v.id)?.let {
                v.fadeOut()
                supportFragmentManager.beginTransaction().hide(it).commit()
            }
        }
        view.fadeIn()
        supportFragmentManager.beginTransaction().show(fragment).commit()
        mShowingGroupName = groupName
    }

    private fun removeTimelineGroup(groupName: String) {
        Logger.v(logTag, "removeTimelineGroup $groupName")
        val viewId = createOrGetTimelineGroupView(groupName).id
        val fragment = supportFragmentManager.findFragmentById(viewId) ?: return
        supportFragmentManager.beginTransaction().remove(fragment).commit()
        columnGroupWrapper.removeView(findViewById(viewId))
        mGroupNameToViewId.remove(groupName)
        if (mShowingGroupName == groupName) showFirstTimelineGroup()
    }

    private fun showFirstTimelineGroup() {
        Logger.v(javaClass.name, "showFirstTimelineGroup")
        val group = mCurrentGroupList.firstOrNull()
        if (group != null) showTimelineGroup(group.name)
        else supportActBar.subtitle = ""
    }

    private fun apply(newGroupList: List<TimelineGroup>) {
        val previousList = mCurrentGroupList.copy()
        mCurrentGroupList.clear()
        mCurrentGroupList.addAll(newGroupList)
        newGroupList.forEach { createOrGetTimelineGroupView(it.name) }
        if (previousList.isEmpty()) {
            showFirstTimelineGroup()
        } else {
            previousList.filter { group -> !newGroupList.any { group.name == it.name } }
                    .also { Logger.v(logTag, "tlChange apply deletions ${it.joinToString(",") { it.name }}") }
                    .forEach { removeTimelineGroup(it.name) }
        }
        if (mCurrentGroupList.isNotEmpty() && mShowingGroupName == null) {
            showFirstTimelineGroup()
        }
    }

    private fun observeTimelineChange() {
        mModel.timelineChange(this) {
            Logger.v(javaClass.name, "timelineChange")
            bindLaunch {
                val result = mModel.loadGroupList().await().result
                apply(result)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val oauthIntent = intent.getParcelableExtra<Intent>(INTENT_OAUTH) ?: return
        val deferred = mModel.onNewIntent(oauthIntent) ?: return
        bindLaunch {
            val res = deferred.await()
            val client = res.orError { toast(R.string.authentication_failure) } ?: return@bindLaunch
            addAccountComponent(client)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(EXTRA_NAVIGATION_STATE, mNavigationState)
        mShowingGroupName?.let { outState.putString(EXTRA_SHOWING_GROUP_NAME, mShowingGroupName) }
        outState.putSerializable(EXTRA_CURRENT_GROUP_LIST, mCurrentGroupList.toTypedArray())
        mCurrentGroupList.forEach { group ->
            val name = group.name
            outState.putInt(EXTRA_GROUP_VIEW_ID + name, mGroupNameToViewId[name]!!)
        }
        outState.putBoolean(EXTRA_IS_FAB_MENU_SHOWING, mIsFabMenuShowing)
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
                createOrGetTimelineGroupView(it.name)
            }
            mShowingGroupName?.let { showTimelineGroup(it) }
            mIsFabMenuShowing = savedInstanceState.getBoolean(EXTRA_IS_FAB_MENU_SHOWING)
            if (mIsFabMenuShowing) showFabMenu()
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
                accountListUI.componentRoot.animate().fadeIn().withEndAction {
                    accountListUI.componentRoot.visibility = View.VISIBLE
                }.start()
            }
            NavigationState.ACCOUNT -> {
                drawable = getDrawable(R.drawable.ic_expand_more_white_24dp)
                mNavigationState = NavigationState.MENU
                navigationHeaderUI.navigationStateIndicator.image = drawable
                accountListUI.componentRoot.visibility = View.GONE
                accountListUI.componentRoot.animate().fadeOut().withEndAction {
                    navigation.menu.setGroupVisible(R.id.main_menu, true)
                }.start()
            }
        }
    }

    private fun showTimelineGroupSelectDialog() {
        val list = mCurrentGroupList
        if (!mInitialized) {
            toast(R.string.message_initialization_not_completed)
            return
        }
        if (list.isEmpty()) {
            toast(R.string.message_group_not_exist)
            return
        }
        TimelineGroupSelectDialog
                .create(list.map { it.name }.let { ArrayList<String>().apply { addAll(it) } })
                .show(supportFragmentManager, TAG_TIMELINE_SELECT_DIALOG)
    }

    private fun showFabMenu() {
        mIsFabMenuShowing = true
        groupChangeFab.animate().translationY(-dip(58).toFloat())
    }

    private fun hideFabMenu() {
        mIsFabMenuShowing = false
        groupChangeFab.animate().translationY(0f)
    }

    private fun onClickTweetFab() {
        if (!mInitialized) {
            toast(R.string.message_initialization_not_completed)
            return
        }
        TweetActivity.start(ctx)
    }

    private fun onDoubleClickTweetFab(): Boolean {
        if (mIsFabMenuShowing) {
            hideFabMenu()
        } else {
            showFabMenu()
        }
        return true
    }

    private fun onClickGroupChangeFab() {
        showTimelineGroupSelectDialog()
        hideFabMenu()
    }

    //interface impl
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.timeline_manage -> TimelineManageActivity.start(this)
            R.id.changing_column_group -> showTimelineGroupSelectDialog()
            R.id.setting -> startLeftOut<SettingsActivity>()
            else -> return super.onOptionsItemSelected(item)
        }
        drawer.closeDrawer(navigation)
        return true
    }

    override fun onDialogItemSelected(requestCode: Int, itemId: Int) {
        fun accountDialog() {
            when (itemId) {
                R.string.add -> startAuthorization()
                R.string.cancel -> showAddAccountDialog()
            }
        }
        when (requestCode) {
            REQUEST_CODE_ADD_ACCOUNT_DIALOG -> accountDialog()
        }
    }

    class TimelineGroupSelectDialog : BottomSheetDialogFragment() {
        private val mGroupNames by lazy { arg.getStringArrayList(EXTRA_GROUP_NAMES) }

        companion object {
            private val EXTRA_GROUP_NAMES = "EXTRA_GROUP_NAMES"

            fun create(groupNames: ArrayList<String>) = TimelineGroupSelectDialog().apply {
                arguments = Bundle().apply {
                    putStringArrayList(EXTRA_GROUP_NAMES, groupNames)
                }
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val dialog = super.onCreateDialog(savedInstanceState)
            fun createMenu(name: String) = createMenuItemUIComponent(ctx, R.drawable.ic_view_carousel_white_24dp, name).let { component ->
                component.componentRoot.also { view ->
                    view.setOnClickListener {
                        (act as MainActivity).showTimelineGroup(name)
                        dismiss()
                    }
                }
            }

            val menus = mGroupNames.map(::createMenu).toTypedArray()
            val component = createBottomSheetUIComponent(act, R.string.select_timeline_group, *menus)
            dialog.setContentView(component.componentRoot)
            return dialog
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