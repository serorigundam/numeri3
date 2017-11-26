package tech.ketc.numeri.ui.activity.tweet

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.Service
import android.arch.lifecycle.ViewModelProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.IBinder
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
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
import tech.ketc.numeri.ui.fragment.dialog.MessageDialogFragment
import tech.ketc.numeri.ui.fragment.dialog.OnDialogItemSelectedListener
import tech.ketc.numeri.ui.model.TweetViewModel
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.android.*
import tech.ketc.numeri.util.anko.component
import tech.ketc.numeri.util.arch.owner.bindLaunch
import tech.ketc.numeri.util.arch.response.nullable
import tech.ketc.numeri.util.arch.response.orError
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import tech.ketc.numeri.util.logTag
import java.io.File
import java.io.IOException
import java.io.Serializable
import javax.inject.Inject

class TweetActivity : AppCompatActivity(), AutoInject, ITweetUI by TweetUI(), TextWatcher, OnDialogItemSelectedListener {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: TweetViewModel by viewModel { mViewModelFactory }
    private val mMediaFiles = ArrayList<File>()

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
    private var mReservedRemovePosition = -1
    private var mIsPossiblySensitive = false
    private var mCurrentText = ""


    companion object {
        private val MAX = 140
        private val TAG_USER_SELECT = "TAG_USER_SELECT"
        private val TAG_REMOVE_THUMB = "TAG_REMOVE_THUMB"
        private val EXTRA_RESERVED_REMOVE_POSITION = "EXTRA_RESERVED_REMOVE_POSITION"
        private val EXTRA_MEDIA_FILES = "EXTRA_MEDIA_FILES"
        private val EXTRA_POSSIBLY_SENSITIVE = "EXTRA_POSSIBLY_SENSITIVE"
        private val EXTRA_CURRENT_TEXT = "EXTRA_CURRENT_TEXT"
        private val REQUEST_REMOVE_THUMB = 100
        private val REQUEST_CODE_SELECT_IMAGE = 200
        private val REQUEST_CODE_READ_STORAGE = 300
        fun start(ctx: Context) {
            ctx.startActivity<TweetActivity>()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        initializeUI()
        initializeUIBehavior()
        savedInstanceState?.let {
            restoreInstanceState(it)
        }
        initialize()
    }

    private fun initializeUI() {
        setUpSupportActionbar(toolbar)
        remainingText.text = "$MAX"
    }

    private fun initializeUIBehavior() {
        toolbar.setFinishWithNavigationClick(this)
        editText.addTextChangedListener(this)
        mediaSelectButton.setOnClickListener { onClickImageSelectButton() }
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

    private fun restoreInstanceState(outState: Bundle) {
        mReservedRemovePosition = outState.getInt(EXTRA_RESERVED_REMOVE_POSITION)
        outState.getStringArrayList(EXTRA_MEDIA_FILES).forEach { addThumb(File(it)) }
        mIsPossiblySensitive = outState.getBoolean(EXTRA_POSSIBLY_SENSITIVE)
        mCurrentText = outState.getString(EXTRA_CURRENT_TEXT)
        editText.setText(mCurrentText)
        editText.setSelection(mCurrentText.length)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(EXTRA_RESERVED_REMOVE_POSITION, mReservedRemovePosition)
        outState.putStringArrayList(EXTRA_MEDIA_FILES,
                arrayListOf(*mMediaFiles.map { it.path }.toTypedArray()))
        outState.putBoolean(EXTRA_POSSIBLY_SENSITIVE, mIsPossiblySensitive)
        outState.putString(EXTRA_CURRENT_TEXT, mCurrentText)
        super.onSaveInstanceState(outState)
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
        tweetService.sendTweet(
                client = clientUser.first,
                clientUser = clientUser.second,
                text = editText.text.toString(),
                mediaList = mMediaFiles,
                isPossiblySensitive = mIsPossiblySensitive,
                inReplyToStatusId = null)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = MenuInflater(ctx)
        inflater.inflate(R.menu.menu_tweet, menu)
        menu.findItem(R.id.check_possibly_sensitive).isChecked = mIsPossiblySensitive
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.check_possibly_sensitive -> {
                mIsPossiblySensitive = !mIsPossiblySensitive
                item.isChecked = mIsPossiblySensitive
            }
            else -> return false
        }
        return true
    }

    private fun onClickImageSelectButton() {
        checkPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_CODE_READ_STORAGE) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }
    }

    private fun addThumb(file: File) {
        if (mMediaFiles.isEmpty()) thumbnailsFrame.visibility = View.VISIBLE
        if (mMediaFiles.size == 4) return
        setTweetSendable(editText.text.toString())
        mMediaFiles.add(file)
        val bitmap = BitmapFactory.decodeFile(file.path)
        val position = mMediaFiles.lastIndex
        thumbnails[position].setImageBitmap(bitmap)
        thumbnails[position].setOnClickListener { removeThumb(position) }
    }

    private fun removeThumb(position: Int) {
        if (position !in 0..4) return
        mReservedRemovePosition = position
        MessageDialogFragment.create(REQUEST_REMOVE_THUMB,
                getString(R.string.exclude_selected_images),
                R.string.exclude).show(supportFragmentManager, TAG_REMOVE_THUMB)
    }

    private fun addMedia(intent: Intent) {
        Logger.v(logTag, "$intent")
        try {
            val file = getImageFile(intent)
            addThumb(file)
        } catch (e: IOException) {
            Logger.printStackTrace(logTag, e)
            toast(R.string.failed_get_image)
        }
    }

    @SuppressLint("Recycle")
    private fun getImageFile(intent: Intent): File {
        val uri = intent.data
        val path = when (uri.scheme) {
            "file" -> uri.path
            "content" -> {
                val frags = intent.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                contentResolver.takePersistableUriPermission(uri, frags)
                val splitIds = DocumentsContract.getDocumentId(uri).split(":")
                val imageId = splitIds.last()
                val cursor = contentResolver
                        .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                arrayOf(MediaStore.Images.Media.DATA),
                                "_id=?", arrayOf(imageId), null) ?: throw IOException()
                if (cursor.moveToFirst()) {
                    cursor.getString(0)?.also { cursor.close() }
                } else {
                    cursor.close()
                    throw IOException()
                }
            }
            else -> throw IOException()
        }
        return File(path)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        if (data == null) return
        when (requestCode) {
            REQUEST_CODE_SELECT_IMAGE -> addMedia(data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size != 1) return
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) return
        when (requestCode) {
            REQUEST_CODE_READ_STORAGE -> onClickImageSelectButton()
        }
    }

    private fun setTweetSendable(text: String) {
        val remaining = MAX - text.length
        if (remaining == 0) {
            tweetSendButton.isEnabled = mMediaFiles.isNotEmpty()
        } else {
            tweetSendButton.isEnabled = remaining in 1..MAX
        }
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
        setTweetSendable(text.toString())
        mCurrentText = text.toString()
        remainingText.text = "${MAX - text.length}"
    }

    override fun onDialogItemSelected(requestCode: Int, itemId: Int) {
        when (requestCode) {
            REQUEST_REMOVE_THUMB -> {
                when (itemId) {
                    R.string.exclude -> {
                        thumbnails[mReservedRemovePosition].setImageDrawable(null)
                        mMediaFiles.removeAt(mReservedRemovePosition)
                        val imageBitmapList = thumbnails.map { (it.image as? BitmapDrawable)?.bitmap }
                                .filter { it != null }
                                .map { it!! }
                        thumbnails.forEach {
                            it.setImageDrawable(null)
                        }
                        imageBitmapList.forEachIndexed { i, bitmap ->
                            thumbnails[i].setImageBitmap(bitmap)
                        }
                        if (mMediaFiles.isEmpty()) {
                            thumbnailsFrame.visibility = View.GONE
                        }
                    }
                }
            }
            else -> {
            }
        }
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