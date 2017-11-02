package net.ketc.numeri.presentation.view.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.util.Log.i
import android.view.View
import android.widget.LinearLayout
import net.ketc.numeri.R
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.model.TwitterUser
import net.ketc.numeri.presentation.presenter.activity.TweetPresenter
import net.ketc.numeri.presentation.view.activity.ui.ITweetActivityUI
import net.ketc.numeri.presentation.view.activity.ui.TweetActivityUI
import net.ketc.numeri.presentation.view.component.ui.AccountViewUI
import net.ketc.numeri.util.toImmutableList
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.nestedScrollView
import java.io.File
import java.io.IOException
import android.graphics.Bitmap
import net.ketc.numeri.util.android.*
import net.ketc.numeri.util.rx.MySchedulers
import android.Manifest.permission.*
import android.content.pm.PackageManager


class TweetActivity
    : ApplicationActivity<TweetPresenter>(), TweetActivityInterface, TextWatcher,
        ITweetActivityUI by TweetActivityUI() {

    override val ctx: Context = this
    override val presenter: TweetPresenter = TweetPresenter(this)

    override var isSendTweetButtonEnabled: Boolean
        get() = sendTweetButton.isEnabled
        set(value) {
            sendTweetButton.isEnabled = value
            sendTweetButton.background = if (value) {
                getDrawable(R.drawable.ripple_button_background)
            } else {
                getDrawable(R.drawable.button_invalid_background)
            }
        }

    private var isInputExcess = false
    private val dialogOwner: DialogOwner = DialogOwner()

    override var text: String
        get() = editText.text.toString()
        set(value) {
            editText.setText(value)
            editText.setSelection(value.count())
        }

    override val defaultClientId: Long by lazy { intent.getLongExtra(EXTRA_CLIENT_ID, -1) }
    override val replyToStatus: Tweet? by lazy { intent.getSerializableExtra(EXTRA_REPLY_TO_STATUS) as? Tweet }
    override val defaultTweetText: String? by lazy { intent.getSerializableExtra(EXTRA_DEFAULT_TWEET_TEXT) as? String }
    private val mMediaList: MutableList<File> = ArrayList()
    override val mediaList: List<File>
        get() = mMediaList.toImmutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        setSupportActionBar(toolBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        initialize()
        savedInstanceState?.getStringArray(EXTRA_MEDIA_PATH_ARRAY)?.forEach {
            addMedia(File(it))
        }
        presenter.initialize(savedInstanceState)
    }

    private fun initialize() {
        remainingText.text = "$MAX_COUNT"
        selectUserButton.setOnClickListener { presenter.onClickSelectUserButton() }
        sendTweetButton.setOnClickListener {
            presenter.sendTweet()
        }
        selectMediaButton.setOnClickListener {
            checkPermissions(READ_EXTERNAL_STORAGE, REQUEST_CODE_STORAGE_ACCESS_SELECT_IMAGE) {
                selectMedia()
            }
        }
        cameraButton.setOnClickListener {
            checkPermissions(READ_EXTERNAL_STORAGE, REQUEST_CODE_STORAGE_ACCESS_CAMERA) {
                executeCamera()
            }
        }
        editText.addTextChangedListener(this)
        defaultTweetText?.let { text = it }
    }

    override fun setUserIcon(user: TwitterUser) {
        selectUserButton.download(user.iconUrl, presenter)
    }

    override fun clear() {
        editText.setText("")
    }

    override fun onPause() {
        super.onPause()
        dialogOwner.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArray(EXTRA_MEDIA_PATH_ARRAY, mMediaList.map { it.path }.toTypedArray())
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        dialogOwner.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        dialogOwner.onDestroy()
    }

    override fun showSelectUserDialog(clientUserList: List<TwitterUser>) {
        var contentsLinear: LinearLayout? = null
        val dialog = AlertDialog.Builder(ctx)
                .setMessage(R.string.select_tweet_user)
                .setView(ctx.nestedScrollView {
                    contentsLinear = linearLayout {
                        orientation = LinearLayout.VERTICAL
                    }
                }).create()

        clientUserList.forEach { user ->
            val ui = AccountViewUI(ctx)
            val contents = contentsLinear ?: throw InternalError()
            contents.addView(ui.createView().apply {
                makeSimpleClickable()
                this.setOnClickListener {
                    presenter.setTweetUser(user.id)
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }
            })
            ui.iconImage.download(user.iconUrl, presenter)
            ui.screenNameText.text = user.screenName
            ui.userNameText.text = user.name
        }
        dialogOwner.showDialog(dialog)
    }

    override fun setRemaining(remaining: Int) {
        if (remaining < 0 && !isInputExcess) {
            isInputExcess = true
            remainingText.textColor = getColor(R.color.color_input_excess)
            isSendTweetButtonEnabled = false
        } else if (remaining >= 0 && isInputExcess) {
            isInputExcess = false
            remainingText.textColor = getColor(getResourceId(android.R.attr.textColorSecondary))
            isSendTweetButtonEnabled = true
        }
        remainingText.text = "$remaining"
    }

    override fun setReplyInfo(info: String) {
        replyInfoText.text = info
    }

    private fun selectMedia() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
    }

    private fun executeCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CODE_CAMERA)
    }

    private fun addMedia(file: File) {
        if (mMediaList.isEmpty())
            thumbnailsFrame.visibility = View.VISIBLE
        if (mMediaList.size == 4) return
        mMediaList.add(file)
        val bitmap = BitmapFactory.decodeFile(file.path)
        val position = mMediaList.lastIndex
        thumbnails[position].setImageBitmap(bitmap)
        thumbnails[position].setOnClickListener {
            removeMedia(position)
        }
    }

    private fun removeMedia(position: Int) {
        if (position !in 0..4) return
        val dialog = AlertDialog.Builder(ctx)
                .setMessage(R.string.exclude_selected_images)
                .setPositiveButton(R.string.yes) { _, _ ->
                    thumbnails[position].setImageDrawable(null)
                    mMediaList.removeAt(position)
                    val imageBitmapList = thumbnails.map { (it.image as? BitmapDrawable)?.bitmap }
                            .filter { it != null }
                            .map { it!! }
                    thumbnails.forEach {
                        it.setImageDrawable(null)
                    }
                    imageBitmapList.forEachIndexed { i, bitmap ->
                        thumbnails[i].setImageBitmap(bitmap)
                    }
                    if (mediaList.isEmpty()) {
                        thumbnailsFrame.visibility = View.GONE
                    }
                }.setNegativeButton(R.string.cancel, null)
                .create()
        dialogOwner.showDialog(dialog)
    }

    private fun addCaptchaMedia(intent: Intent?) {
        if (intent != null) {
            val bitmap = (intent.extras.get("data") as? Bitmap)
                    ?: throw ImageAcquisitionFailureException()
            presenter.singleTask(MySchedulers.io) {
                bitmap.save(ctx)
            } error {
                toast("画像の取得に失敗")
            } success {
                addMedia(it)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                REQUEST_CODE_SELECT_IMAGE -> addMedia(data)
                REQUEST_CODE_CAMERA -> addCaptchaMedia(data)
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size != 1) return
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                REQUEST_CODE_STORAGE_ACCESS_SELECT_IMAGE -> selectMedia()
                REQUEST_CODE_STORAGE_ACCESS_CAMERA -> executeCamera()
            }
        } else {
            toast(getString(R.string.message_not_granted_permission))
        }
    }

    private fun addMedia(intent: Intent?) {
        if (intent != null) {
            val uri = intent.data
            i("", "Uri: " + uri.toString())
            try {
                val file = getImageFile(intent)
                addMedia(file)
            } catch (e: IOException) {
                e.printStackTrace()
                toast("画像の取得に失敗")
            }
        }
    }

    private fun getImageFile(intent: Intent): File {
        val uri = intent.data
        val path: String = if (uri.scheme == "file") {
            uri.path
        } else if (uri.scheme == "content") {
            val frags = intent.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            contentResolver.takePersistableUriPermission(uri, frags)
            val splitIds = DocumentsContract.getDocumentId(uri).split(":")
            val imageId = splitIds[splitIds.lastIndex]
            val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Images.Media.DATA), "_id=?", arrayOf(imageId), null)
            cursor?.let {
                if (cursor.moveToFirst()) {
                    cursor.getString(0)?.also {
                        cursor.close()
                    }
                } else throw ImageAcquisitionFailureException()
            } ?: throw ImageAcquisitionFailureException()
        } else throw ImageAcquisitionFailureException()
        return File(path)
    }


    override fun afterTextChanged(s: Editable) {
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        presenter.checkRemaining()
    }

    class ImageAcquisitionFailureException : IOException()

    companion object {
        val MAX_COUNT = 140
        val EXTRA_REPLY_TO_STATUS = "EXTRA_REPLY_TO_STATUS"
        val EXTRA_CLIENT_ID = "EXTRA_CLIENT_ID"
        val EXTRA_MEDIA_PATH_ARRAY = "EXTRA_MEDIA_PATH_ARRAY"
        val EXTRA_DEFAULT_TWEET_TEXT = "EXTRA_DEFAULT_TWEET_TEXT"
        val REQUEST_CODE_SELECT_IMAGE = 100
        val REQUEST_CODE_CAMERA = 200
        val REQUEST_CODE_STORAGE_ACCESS_SELECT_IMAGE = 300
        val REQUEST_CODE_STORAGE_ACCESS_CAMERA = 400

        fun start(ctx: Context, clientId: Long? = null, replyTo: Tweet? = null, defaultTweetText: String? = null) {
            val list = ArrayList<Pair<String, Any>>()
            list.add(EXTRA_CLIENT_ID to (clientId ?: -1))
            replyTo?.let {
                list.add(EXTRA_REPLY_TO_STATUS to it)
            }
            defaultTweetText?.let {
                list.add(EXTRA_DEFAULT_TWEET_TEXT to defaultTweetText)
            }
            ctx.startActivity<TweetActivity>(*list.toTypedArray())
        }
    }
}

interface TweetActivityInterface : ActivityInterface {
    var isSendTweetButtonEnabled: Boolean
    val replyToStatus: Tweet?
    val defaultTweetText: String?
    val defaultClientId: Long
    var text: String
    val mediaList: List<File>
    fun setUserIcon(user: TwitterUser)
    fun clear()
    fun finish()
    fun showSelectUserDialog(clientUserList: List<TwitterUser>)
    fun setRemaining(remaining: Int)
    fun setReplyInfo(info: String)
}