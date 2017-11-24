package tech.ketc.numeri.ui.fragment.timelinemanage

import android.annotation.SuppressLint
import android.app.Dialog
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.twitter.model.UserList
import tech.ketc.numeri.infra.element.TlType
import tech.ketc.numeri.infra.entity.TimelineGroup
import tech.ketc.numeri.infra.entity.TimelineInfo
import tech.ketc.numeri.ui.activity.timelinemanage.OnAddFabClickListener
import tech.ketc.numeri.ui.components.IRecyclerUIComponent
import tech.ketc.numeri.ui.components.RecyclerUIComponent
import tech.ketc.numeri.ui.fragment.overlay.ProgressFragment
import tech.ketc.numeri.ui.model.TimelineManageViewModel
import tech.ketc.numeri.util.android.*
import tech.ketc.numeri.util.android.ui.recycler.SimpleItemTouchHelper
import tech.ketc.numeri.util.android.ui.recycler.SimpleRecyclerAdapter
import tech.ketc.numeri.util.anko.UIComponent
import tech.ketc.numeri.util.anko.marginTop
import tech.ketc.numeri.util.arch.owner.bindLaunch
import tech.ketc.numeri.util.arch.response.orError
import tech.ketc.numeri.util.arch.viewmodel.commonViewModel
import tech.ketc.numeri.util.di.AutoInject
import javax.inject.Inject

class TimelineManageFragment : Fragment(), AutoInject, OnAddFabClickListener,
        IRecyclerUIComponent by RecyclerUIComponent() {
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: TimelineManageViewModel by commonViewModel { mViewModelFactory }

    private val TimelineInfo.name
        get() = mModel.toName(this)

    private val mGroupName by lazy { arg.getString(EXTRA_GROUP_NAME) }

    private val mAdapter = SimpleRecyclerAdapter<TimelineInfo>({ it.name }, {
        onItemClick(it)
    })
    private var mInitialized = false

    companion object {
        private val EXTRA_GROUP_NAME = "EXTRA_GROUP_NAME"
        private val EXTRA_INITIALIZED = "EXTRA_INITIALIZED"
        private val TAG_PROGRESS = "TAG_PROGRESS"
        private val TAG_INFO_CREATE = "TAG_INFO_CREATE"

        fun create(group: TimelineGroup) = TimelineManageFragment().apply {
            arguments = Bundle().apply {
                putString(EXTRA_GROUP_NAME, group.name)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = createView(act)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            mInitialized = it.getBoolean(EXTRA_INITIALIZED)
        }
        if (!mInitialized) showProgress()
        mModel.initialize(this, {
            mInitialized = true
            hideProgress()
            initialize()
        }, {
            toast(getString(R.string.error_not_get_timeline_name_info))
        })
    }

    private fun showProgress() {
        childFragmentManager.beginTransaction()
                .replace(root.id, ProgressFragment(), TAG_PROGRESS)
                .commit()
    }

    private fun hideProgress() {
        val fragment = childFragmentManager.findFragmentByTag(TAG_PROGRESS) ?: return
        childFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
    }


    fun initialize() {
        bindLaunch {
            val infoList = mModel.loadTimelineInfoList(mGroupName).await().result
            recycler.adapter = mAdapter.apply { values.addAll(infoList) }
            initializeUIBehavior()
        }
    }

    private fun initializeUIBehavior() {
        SimpleItemTouchHelper(swipeEnable = true, onSwiped = { viewHolder, _ ->
            val values = mAdapter.values
            val position = viewHolder.adapterPosition
            if (position > values.lastIndex) return@SimpleItemTouchHelper
            bindLaunch {
                val info = values[position]
                mModel.removeFromGroup(mGroupName, info).await().orError {
                    toast(R.string.error_delete_failure)
                    mAdapter.notifyItemChanged(position)
                } ?: return@bindLaunch
                values.removeAt(position)
                mAdapter.notifyItemRemoved(position)
                showUndoDeleteSnackbar(info, position)
            }
        }, moveEnable = true, onMove = { _, fromHolder, targetHolder ->
            val values = mAdapter.values
            val targetPosition = targetHolder.adapterPosition
            val fromPosition = fromHolder.adapterPosition
            if (targetPosition > values.lastIndex || fromPosition > values.lastIndex)
                return@SimpleItemTouchHelper false
            bindLaunch {
                mModel.replace(mGroupName, values[fromPosition], values[targetPosition]).await()
                val temp = values[fromPosition]
                values[fromPosition] = values[targetPosition]
                values[targetPosition] = temp
                mAdapter.notifyItemMoved(fromPosition, targetPosition)
            }
            true
        }).attachToRecyclerView(recycler)
    }

    private fun showUndoDeleteSnackbar(info: TimelineInfo, position: Int) {
        val snackbar = (snackbarMaker ?: return)
                .make(getString(R.string.message_delete) + "[${info.name}]", Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction(R.string.undo) {
            bindLaunch {
                mModel.insert(mGroupName, info, position).await().orError {
                    toast(getString(R.string.error_regenerate_failure))
                }
                mAdapter.values.add(position, info)
                mAdapter.notifyItemInserted(position)
            }
        }
        snackbar.show()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(EXTRA_INITIALIZED, mInitialized)
        super.onSaveInstanceState(outState)
    }

    private fun onItemClick(info: TimelineInfo) {
        toast(mModel.toName(info))
    }

    private fun check(info: TimelineInfo) = !mAdapter.values.any {
        info.type == it.type
                && info.accountId == it.accountId
                && info.foreignId == it.foreignId
    }

    private fun onPositiveClick(info: TimelineInfo) {
        bindLaunch {
            val timelineInfo = mModel.joinToGroup(mGroupName, info).await().orError {
                toast(R.string.error_generate_timeline_failure)
            } ?: return@bindLaunch
            val values = mAdapter.values
            values.add(timelineInfo)
            mAdapter.notifyItemInserted(values.size)
        }
    }

    override fun onAddFabClick() {
        if (!mInitialized) return
        TimelineInfoCreateDialogFragment
                .create(mModel.clientUsers.map { Triple(it.first.id, it.second.screenName, mModel.userList(it.first).toTypedArray()) })
                .show(childFragmentManager, TAG_INFO_CREATE)
    }

    private interface IInfoCreateUIComponent : UIComponent<RelativeLayout> {
        val radioGroup: RadioGroup
        val accountSpinner: Spinner
        val userListText: TextView
        val userListSpinner: Spinner
        val typeErrorText: TextView
        val listErrorText: TextView
    }

    private class InfoCrateUIComponent : IInfoCreateUIComponent {
        override lateinit var radioGroup: RadioGroup
            private set
        override lateinit var accountSpinner: Spinner
            private set
        override lateinit var userListText: TextView
            private set
        override lateinit var userListSpinner: Spinner
            private set
        override lateinit var typeErrorText: TextView
            private set
        override lateinit var listErrorText: TextView
            private set

        @SuppressLint("SetTextI18n")
        override fun createView(ctx: Context) = ctx.relativeLayout {
            lparams(wrapContent, matchParent) {
                setPadding(dimen(R.dimen.margin_large),
                        dimen(R.dimen.margin_medium),
                        dimen(R.dimen.margin_large),
                        dimen(R.dimen.margin_medium))
            }
            relativeLayout {
                id = R.id.radio_content
                radioGroup {
                    radioGroup = this
                    id = R.id.radio_group
                    val init: RadioGroup.LayoutParams.() -> Unit = {
                        weight = 1f
                        marginEnd = dimen(R.dimen.margin_small)
                    }
                    orientation = LinearLayout.HORIZONTAL
                    radioButton {
                        id = R.id.home_radio
                        text = "Home"
                    }.lparams { init() }
                    radioButton {
                        id = R.id.mentions_radio
                        text = "Mentions"
                    }.lparams { init() }
                    radioButton {
                        id = R.id.list_radio
                        text = "List"
                    }.lparams { weight = 1f }
                }

                textView {
                    typeErrorText = this
                    visibility = View.GONE
                    isFocusableInTouchMode = true
                }.lparams(dip(1), dip(4)) {
                    endOf(R.id.radio_group)
                    marginStart = dimen(R.dimen.margin_medium)
                    sameBottom(R.id.radio_group)
                }
            }

            textView("account") {
                id = R.id.account_text
            }.lparams(matchParent, wrapContent) {
                below(R.id.radio_content)
                marginTop = dimen(R.dimen.margin_medium)
            }
            spinner {
                accountSpinner = this
                id = R.id.account_spinner
            }.lparams(matchParent, wrapContent) {
                below(R.id.account_text)
                marginTop = dimen(R.dimen.margin_text_medium)
            }

            textView("userList") {
                userListText = this
                id = R.id.user_list_text
                visibility = View.GONE
            }.lparams(matchParent, wrapContent) {
                below(R.id.account_spinner)
                marginTop = dimen(R.dimen.margin_medium)
            }
            relativeLayout {
                spinner {
                    userListSpinner = this
                    id = R.id.user_list_spinner
                    visibility = View.GONE
                }.lparams(matchParent, wrapContent) {
                    centerVertically()
                    alignParentStart()
                }
                textView {
                    listErrorText = this
                    visibility = View.INVISIBLE
                    isFocusableInTouchMode = true
                }.lparams(dip(1), dip(4)) {
                    alignParentEnd()
                    sameBottom(R.id.user_list_spinner)
                }
            }.lparams {
                below(R.id.user_list_text)
                marginTop = dimen(R.dimen.margin_text_medium)
            }
        }
    }


    class TimelineInfoCreateDialogFragment : DialogFragment(), IInfoCreateUIComponent by InfoCrateUIComponent() {
        private var mCheckedRadioId = -1
        private var mSelectedClientId: Long = -1
        private var mSelectedForeignId: Long = -1
        private val mAccountInfo by lazy {
            val tmp = arg.getSerializable(EXTRA_CLIENT_ID_TO_SCREEN_NAME_LIST) as Array<*>
            Array(tmp.size) {
                val value = tmp[it] as Triple<*, *, *>
                val listArray = value.third as Array<*>
                val lists = Array(listArray.size) { listArray[it] as UserList }
                Triple(value.first as Long, value.second as String, lists)
            }
        }
        private val mAccountAdapter by lazy { SimpleArrayAdapter<Pair<Long, String>>(act, { it.second }) }
        private val mUserListAdapter by lazy { SimpleArrayAdapter<UserList>(act, { it.name }) }
        private lateinit var dialog: AlertDialog

        companion object {
            private val EXTRA_CHECKED_RADIO_ID = "EXTRA_CHECKED_RADIO_ID"
            private val EXTRA_SELECTED_CLIENT_ID = "EXTRA_SELECTED_CLIENT_ID"
            private val EXTRA_SELECTED_FOREIGN_ID = "EXTRA_SELECTED_FOREIGN_ID"
            private val EXTRA_CLIENT_ID_TO_SCREEN_NAME_LIST = "EXTRA_CLIENT_ID_TO_SCREEN_NAME_LIST"
            fun create(accountInfo: List<Triple<Long, String, Array<UserList>>>) = TimelineInfoCreateDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_CLIENT_ID_TO_SCREEN_NAME_LIST, accountInfo.toTypedArray())
                }
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val view = createView(ctx)
            restore(savedInstanceState)
            dialog = AlertDialog.Builder(context!!)
                    .setMessage(R.string.message_create_timeline_info)
                    .setView(view)
                    .setPositiveButton(R.string.add) { _, _ ->
                        val type = toType(mCheckedRadioId)
                        val info = if (mSelectedForeignId != (-1).toLong()) TimelineInfo(type = type,
                                accountId = mSelectedClientId, foreignId = mSelectedForeignId)
                        else TimelineInfo(type = type, accountId = mSelectedClientId)
                        (parentFragment as TimelineManageFragment).onPositiveClick(info)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .create()
            radioGroup.init()
            userListInit()
            accountSpinnerInit()
            return dialog
        }


        private fun Bundle.restoreParams() {
            mCheckedRadioId = getInt(EXTRA_CHECKED_RADIO_ID)
            mSelectedClientId = getLong(EXTRA_SELECTED_CLIENT_ID)
            mSelectedForeignId = getLong(EXTRA_SELECTED_FOREIGN_ID)
        }

        private fun restore(savedInstanceState: Bundle?) {
            if (savedInstanceState == null) return
            savedInstanceState.restoreParams()
            if (mCheckedRadioId != -1) radioGroup.check(mCheckedRadioId)
            if (mSelectedClientId != (-1).toLong()) {
                accountSpinner.setSelection(mAccountInfo.indexOfFirst { it.first == mSelectedClientId })
            }
            if (mSelectedForeignId != (-1).toLong()) {
                setUserListVisibility(true)
                accountSpinner.setSelection(mAccountInfo.find { it.first == mSelectedClientId }!!
                        .third.indexOfFirst { it.id == mSelectedForeignId })
            }
        }

        private fun toType(id: Int): TlType = when (id) {
            R.id.home_radio -> TlType.HOME
            R.id.mentions_radio -> TlType.MENTIONS
            R.id.list_radio -> TlType.USER_LIST
            else -> throw RuntimeException()
        }

        private fun TextView.setErrorText(error: String) {
            if (error.isNotEmpty()) {
                this.setError(error, null)
                this.visibility = View.VISIBLE
            } else {
                this.visibility = View.INVISIBLE
            }
        }

        private fun check(): Boolean {
            val parent = parentFragment as TimelineManageFragment
            if (mSelectedClientId == (-1).toLong() || mCheckedRadioId == -1) {
                typeErrorText.setErrorText(getString(R.string.error_need_to_select_timeline_type))
                return false
            } else {
                typeErrorText.setErrorText("")
            }
            if (mCheckedRadioId == R.id.list_radio && mSelectedForeignId == (-1).toLong()) {
                listErrorText.setErrorText(getString(R.string.error_need_to_select_user_list))
                return false
            } else {
                listErrorText.setErrorText("")
            }
            return (if (mCheckedRadioId == R.id.list_radio) parent.check(TimelineInfo(type = toType(mCheckedRadioId),
                    accountId = mSelectedClientId, foreignId = mSelectedForeignId))
            else parent.check(TimelineInfo(type = toType(mCheckedRadioId),
                    accountId = mSelectedClientId))).also {
                if (it) typeErrorText.setErrorText("")
                else {
                    typeErrorText.setErrorText(getString(R.string.error_already_exist))
                }
            }
        }

        private fun checkPositiveEnabled() {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.isEnabled = check()
        }

        private fun setUserListVisibility(visible: Boolean) {
            if (visible) {
                userListText.visibility = View.VISIBLE
                userListSpinner.visibility = View.VISIBLE
                userListText.fadeIn()
                userListSpinner.fadeIn()
            } else {
                userListText.visibility = View.GONE
                userListSpinner.visibility = View.GONE
                listErrorText.visibility = View.GONE
                mSelectedForeignId = -1
                userListText.fadeOut()
                userListSpinner.fadeOut()
            }
        }

        private fun RadioGroup.init() {
            setOnCheckedChangeListener { _, id ->
                mCheckedRadioId = id
                when (id) {
                    R.id.home_radio -> {
                        setUserListVisibility(false)
                    }
                    R.id.mentions_radio -> {
                        setUserListVisibility(false)
                    }
                    R.id.list_radio -> {
                        setUserListVisibility(true)
                    }
                }
                checkPositiveEnabled()
            }
        }

        private fun accountSpinnerInit() {
            mAccountAdapter.addAll(mAccountInfo.map { it.first to it.second })
            accountSpinner.adapter = mAccountAdapter
            accountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>, p1: View?, position: Int, id: Long) {
                    val item = mAccountAdapter.getItem(position)
                    mSelectedClientId = item.first
                    mUserListAdapter.clear()
                    mUserListAdapter.addAll(mAccountInfo.find { it.first == item.first }!!.third.toList())
                    checkPositiveEnabled()
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {
                }
            }
        }

        private fun userListInit() {
            mUserListAdapter.addAll(mAccountInfo.first().third.toMutableList())
            userListSpinner.adapter = mUserListAdapter
            userListSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>, p1: View?, position: Int, id: Long) {
                    val item = mUserListAdapter.getItem(position)
                    mSelectedForeignId = item.id
                    checkPositiveEnabled()
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {
                }
            }
        }

        override fun onResume() {
            super.onResume()
            checkPositiveEnabled()
        }


        override fun onSaveInstanceState(outState: Bundle) {
            outState.putInt(EXTRA_CHECKED_RADIO_ID, mCheckedRadioId)
            outState.putLong(EXTRA_SELECTED_CLIENT_ID, mSelectedClientId)
            outState.putLong(EXTRA_SELECTED_FOREIGN_ID, mSelectedForeignId)
            super.onSaveInstanceState(outState)
        }

        private class SimpleArrayAdapter<T>(context: Context, private val transform: (T) -> String)
            : ArrayAdapter<T>(context, 0) {

            private var currentSelectItem: T? = null
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, null)
                val item = getItem(position)
                currentSelectItem = item
                view.findViewById<TextView>(android.R.id.text1).text = transform(item)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val view = convertView ?: context.relativeLayout {
                    lparams(matchParent, wrapContent)
                    backgroundResource = context.getResourceId(android.R.attr.panelColorBackground)
                    textView {
                        id = R.id.text
                    }.lparams(matchParent, wrapContent) {
                        margin = dimen(R.dimen.margin_text_medium)
                        centerInParent()
                    }
                }
                val textView: TextView = view.findViewById(R.id.text)
                val item = getItem(position)
                currentSelectItem?.let {
                    if (item == it) textView.textColorResource = R.color.colorAccent
                    else textView.textColor = context.getColor(context.getResourceId(android.R.attr.textColorSecondary))
                }
                textView.text = transform(item)
                return view
            }
        }
    }
}