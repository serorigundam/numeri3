package net.ketc.numeri.presentation.view.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.NavigationView
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import net.ketc.numeri.R
import net.ketc.numeri.domain.entity.TweetsDisplayGroup
import net.ketc.numeri.domain.model.TwitterUser
import net.ketc.numeri.presentation.presenter.activity.MainPresenter
import net.ketc.numeri.presentation.view.activity.ui.IMainActivityUI
import net.ketc.numeri.presentation.view.activity.ui.MainActivityUI
import net.ketc.numeri.presentation.view.component.ui.menu.createIconMenu
import net.ketc.numeri.presentation.view.component.ui.dialog.BottomSheetDialogUI
import net.ketc.numeri.presentation.view.component.ui.dialog.addMenu
import net.ketc.numeri.presentation.view.component.ui.dialog.messageText
import net.ketc.numeri.presentation.view.fragment.TimeLinesFragment
import net.ketc.numeri.util.android.*
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.toImmutableList
import org.jetbrains.anko.*
import java.util.*

class MainActivity : ApplicationActivity<MainPresenter>(),
        NavigationView.OnNavigationItemSelectedListener,
        MainActivityInterface, IMainActivityUI by MainActivityUI() {
    override val ctx: Context
        get() = this
    override val presenter: MainPresenter = MainPresenter(this)

    private val drawerToggle: ActionBarDrawerToggle by lazy { ActionBarDrawerToggle(this, drawer, 0, 0) }
    override var showingGroupId = -1
        private set

    private val accountItemViewHolderList = ArrayList<AccountItemViewHolder>()
    private val groups = ArrayList<TweetsDisplayGroup>()
    private val dialogOwner = DialogOwner()

    override var addAccountButtonEnabled: Boolean
        get() = addAccountButton.isEnabled
        set(value) {
            addAccountButton.isEnabled = value
        }

    override val accounts: List<TwitterUser>
        get() = mAccounts.toImmutableList()

    private val mAccounts = ArrayList<TwitterUser>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        initialize()
        presenter.initialize(savedInstanceState)
    }

    private fun initialize() {
        setSupportActionBar(find<Toolbar>(R.id.toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        drawer.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        navigation.setNavigationItemSelectedListener(this)
        showAccountRelative.setOnClickListener {
            toggleNavigationState()
        }

        addAccountButton.setOnClickListener { presenter.newAuthenticate() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val oauthIntent = intent.getParcelableExtra<Intent>(INTENT_OAUTH) ?: return
        presenter.onNewIntent(oauthIntent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.column_manage -> {
                presenter.startTweetsDisplayGroupManageActivity()
            }
            R.id.changing_column_group -> {
                showChangeColumnGroupDialog()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        drawer.closeDrawer(navigation)
        return true
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

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onPause() {
        super.onPause()
        dialogOwner.onPause()
    }

    override fun onResume() {
        super.onResume()
        dialogOwner.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        dialogOwner.onDestroy()
    }

    private fun toggleNavigationState() {
        if (navigationContent.visibility == View.GONE) {
            navigation.menu.setGroupVisible(R.id.main_menu, false)
            showAccountIndicator.image = getDrawable(R.drawable.ic_expand_less_white_24dp)
            navigationContent.visibility = View.VISIBLE
            navigationContent.fadeIn()
        } else if (navigationContent.visibility == View.VISIBLE) {
            showAccountIndicator.image = getDrawable(R.drawable.ic_expand_more_white_24dp)
            navigationContent.visibility = View.GONE
            navigationContent.fadeOut()
            navigation.menu.setGroupVisible(R.id.main_menu, true)
        }
    }

    fun addGroupView(id: Int) {
        columnGroupWrapper.addView(ctx.frameLayout {
            this.id = id
            tag = id.toString()
            visibility = View.GONE
            lparams(matchParent, matchParent)
        })
    }


    override fun addAccount(twitterUser: TwitterUser, autoDisposable: AutoDisposable) {
        val holder = AccountItemViewHolder(ctx, twitterUser, autoDisposable)
        accountItemViewHolderList.add(holder)
        accountsLinear.addView(holder.view)
        holder.view.fadeIn()
        mAccounts.add(twitterUser)
        Handler().postDelayed({
            toggleNavigationState()
            drawer.closeDrawer(navigation)
        }, 600)
    }

    override fun updateAccount(user: TwitterUser) {
        val clientHolder = accountItemViewHolderList.find { it.twitterUser == user }
                ?: throw IllegalArgumentException("nonexistent user was passed")
        clientHolder.update()
    }

    override fun addGroup(group: TweetsDisplayGroup) {
        if (groups.contains(group))
            return
        addGroupView(group.id)
        groups.add(group)
        supportFragmentManager.beginTransaction()
                .replace(group.id, TimeLinesFragment.create(group), group.id.toString())
                .commit()
    }

    override fun removeGroup(group: TweetsDisplayGroup) {
        if (!groups.contains(group))
            return
        val removed = (0..columnGroupWrapper.childCount)
                .map { columnGroupWrapper.getChildAt(it) }
                .find { it.id == group.id } ?: throw InternalError()
        val removedFragment = supportFragmentManager.fragments
                .filter { it is TimeLinesFragment }
                .map { it as TimeLinesFragment }
                .find { it.group.id == group.id }
        supportFragmentManager.beginTransaction()
                .remove(removedFragment)
                .commit()
        columnGroupWrapper.removeView(removed)
        if (removed.id == showingGroupId) {
            columnGroupWrapper.getChildAt(0)?.let {
                it.visibility = View.VISIBLE
                showingGroupId = it.id
            }
        }
        groups.remove(group)
    }

    override fun showGroup(group: TweetsDisplayGroup) {
        columnGroupWrapper.forEachChild {
            if (it.id == group.id) {
                showingGroupId = group.id
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
        supportActionBar!!.subtitle = group.name
    }

    override fun showAddAccountDialog() {
        var ok = false
        val dialog = AlertDialog.Builder(ctx)
                .setPositiveButton(getString(R.string.yes), { _, _ ->
                    drawer.openDrawer(navigation)
                    toggleNavigationState()
                    ok = true
                })
                .setOnDismissListener { if (!ok) showAddAccountDialog() }
                .setMessage("アカウントの認証する必要があります。")
                .create()
        dialogOwner.showDialog(dialog)
    }

    fun showChangeColumnGroupDialog() {
        val dialog = BottomSheetDialog(this)
        dialog.apply {
            setContentView(BottomSheetDialogUI(ctx).createView())
            groups.forEach { group ->
                addMenu(createIconMenu(ctx, R.drawable.ic_view_carousel_white_24dp, group.name, {
                    showGroup(group)
                    dialog.dismiss()
                }))
                messageText.text = getString(R.string.select_column_group)
            }
        }
        dialogOwner.showDialog(dialog)
    }

    class AccountItemViewHolder(ctx: Context, val twitterUser: TwitterUser, val autoDisposable: AutoDisposable) {
        val view: View = createAccountItem(ctx)

        init {
            update()
        }

        fun update() {
            view.find<TextView>(R.id.screen_name_text).text = twitterUser.screenName
            view.find<TextView>(R.id.user_name_text).text = twitterUser.name
            view.find<ImageView>(R.id.icon_image).download(twitterUser.iconUrl, autoDisposable)
        }


        companion object {
            private fun createAccountItem(ctx: Context) = ctx.relativeLayout {
                lparams(matchParent, wrapContent) {
                    padding = dimen(R.dimen.margin_medium)
                    gravity = Gravity.CENTER_VERTICAL
                }
                backgroundResource = context.getResourceId(android.R.attr.selectableItemBackground)
                isClickable = true

                imageView {
                    id = R.id.icon_image
                    backgroundColor = ctx.getColor(R.color.image_background_transparency)
                }.lparams(dimen(R.dimen.image_icon), dimen(R.dimen.image_icon)) {
                    marginEnd = dimen(R.dimen.margin_medium)
                }

                textView {
                    id = R.id.user_name_text
                    ellipsize = TextUtils.TruncateAt.END
                    lines = 1
                    textColor = ctx.getColor(ctx.getResourceId(android.R.attr.textColorPrimary))
                    textSizeDimen = R.dimen.text_size_medium
                }.lparams {
                    marginEnd = dimen(R.dimen.margin_medium)
                    sameTop(R.id.icon_image)
                    rightOf(R.id.icon_image)
                }
                textView {
                    id = R.id.screen_name_text
                    ellipsize = TextUtils.TruncateAt.END
                    lines = 1
                    textSizeDimen = R.dimen.text_size_medium
                }.lparams {
                    marginEnd = dimen(R.dimen.margin_medium)
                    sameBottom(R.id.icon_image)
                    rightOf(R.id.icon_image)
                    below(R.id.user_name_text)
                }
            }
        }

    }

    companion object {
        val INTENT_OAUTH = "INTENT_OAUTH"
    }
}


interface MainActivityInterface : ActivityInterface {
    var addAccountButtonEnabled: Boolean
    val accounts: List<TwitterUser>
    val showingGroupId: Int
    fun addAccount(twitterUser: TwitterUser, autoDisposable: AutoDisposable)
    fun updateAccount(user: TwitterUser)
    fun addGroup(group: TweetsDisplayGroup)
    fun removeGroup(group: TweetsDisplayGroup)
    fun showGroup(group: TweetsDisplayGroup)
    fun showAddAccountDialog()
}

class OauthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity<MainActivity>(MainActivity.INTENT_OAUTH to intent)
        finish()
    }
}