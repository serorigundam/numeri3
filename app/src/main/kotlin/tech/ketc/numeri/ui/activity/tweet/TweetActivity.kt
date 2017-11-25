package tech.ketc.numeri.ui.activity.tweet

import android.app.Dialog
import android.app.Service
import android.arch.lifecycle.ViewModelProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import android.widget.LinearLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.nestedScrollView
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.service.ITweetService
import tech.ketc.numeri.service.TweetService
import tech.ketc.numeri.ui.components.AccountUIComponent
import tech.ketc.numeri.ui.model.TweetViewModel
import tech.ketc.numeri.util.android.act
import tech.ketc.numeri.util.android.arg
import tech.ketc.numeri.util.android.setFinishWithNavigationClick
import tech.ketc.numeri.util.android.setUpSupportActionbar
import tech.ketc.numeri.util.anko.component
import tech.ketc.numeri.util.arch.owner.bindLaunch
import tech.ketc.numeri.util.arch.response.nullable
import tech.ketc.numeri.util.arch.response.orError
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import java.io.Serializable
import javax.inject.Inject

class TweetActivity : AppCompatActivity(), AutoInject, ITweetUI by TweetUI(), TextWatcher {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: TweetViewModel by viewModel { mViewModelFactory }

    private var mClientUser: Pair<TwitterClient, TwitterUser>? = null
    private fun clientUser() = mClientUser ?: throw IllegalStateException()
    private var serviceStarted = false
    private lateinit var tweetService: ITweetService
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            service ?: return
            tweetService = (service as ITweetService.Binder).getService()
            serviceStarted = true
        }
    }


    companion object {
        private val MAX = 140
        private val TAG_USER_SELECT = "TAG_USER_SELECT"
        fun start(ctx: Context) {
            ctx.startActivity<TweetActivity>()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        initializeUI()
        initializeUIBehavior()
        initialize()
    }

    private fun initializeUI() {
        setUpSupportActionbar(toolbar)
        remainingText.text = "$MAX"
    }

    private fun initializeUIBehavior() {
        toolbar.setFinishWithNavigationClick(this)
        editText.addTextChangedListener(this)
    }

    private fun initialize() {
        bindLaunch {
            val clients = mModel.clients().await().orError {
                toast(R.string.authentication_failure)
            } ?: return@bindLaunch
            val clientUsers = mModel.getClientUsers(clients).await().orError {
                toast(R.string.message_failed_user_info)
            } ?: return@bindLaunch
            val intent = Intent(this@TweetActivity, TweetService::class.java)
            startService(intent)
            bindService(intent, connection, Service.BIND_AUTO_CREATE)
            setTweetUser(clientUsers.first())
            setUserSelectEvent(clientUsers)
            tweetSendButton.setOnClickListener { onClickTweetButton() }
        }
    }

    private fun setUserSelectEvent(clientUsers: List<Pair<TwitterClient, TwitterUser>>) {
        userSelectButton.setOnClickListener {
            UserSelectDialogFragment.create(clientUsers).show(supportFragmentManager, TAG_USER_SELECT)
        }
    }

    private fun setTweetUser(clientUser: Pair<TwitterClient, TwitterUser>) {
        if (mClientUser != null && clientUser().first.id == clientUser.first.id) return
        mClientUser = clientUser
        val user = clientUser.second
        bindLaunch {
            val content = mModel.loadImage(user.iconUrl).await().nullable() ?: return@bindLaunch
            userSelectButton.setImageBitmap(content.bitmap)
        }
    }

    private fun onClickTweetButton() {
        val clientUser = clientUser()
        tweetService.sendTweet(clientUser.first, clientUser.second, editText.text.toString())
        finish()
    }

    override fun onDestroy() {
        unbindService(connection)
        super.onDestroy()
    }

    override fun afterTextChanged(text: Editable) {
    }

    override fun beforeTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {
        val remaining = MAX - text.length
        tweetSendButton.isEnabled = remaining != 0 && remaining <= MAX && serviceStarted
        remainingText.text = "$remaining"
    }

    class UserSelectDialogFragment : DialogFragment() {
        private val mClientUsers by lazy { (arg.getSerializable(EXTRA_CLIENT_USERS) as Data).clientUsers }
        private val tweetAct by lazy { (act as TweetActivity) }
        private val mModel by lazy { tweetAct.mModel }

        companion object {
            private val EXTRA_CLIENT_USERS = "EXTRA_CLIENT_USERS"
            fun create(clientUsers: List<Pair<TwitterClient, TwitterUser>>) = UserSelectDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_CLIENT_USERS, Data(clientUsers))
                }
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            fun ImageView.setUrl(url: String) {
                bindLaunch {
                    val content = mModel.loadImage(url).await().nullable() ?: return@bindLaunch
                    setImageBitmap(content.bitmap)
                }
            }

            val view = ctx.nestedScrollView {
                linearLayout {
                    orientation = LinearLayout.VERTICAL
                    mClientUsers.forEach { clientUser ->
                        val component = AccountUIComponent()
                        component(component) {
                            val user = clientUser.second
                            component.iconImage.setUrl(user.iconUrl)
                            component.screenNameText.text = user.screenName
                            component.userNameText.text = user.name
                            setOnClickListener {
                                tweetAct.setTweetUser(clientUser)
                                dismiss()
                            }
                        }
                    }
                }
            }

            return AlertDialog.Builder(ctx)
                    .setMessage(R.string.select_tweet_user)
                    .setView(view)
                    .create()
        }

        data class Data(val clientUsers: List<Pair<TwitterClient, TwitterUser>>) : Serializable
    }

}