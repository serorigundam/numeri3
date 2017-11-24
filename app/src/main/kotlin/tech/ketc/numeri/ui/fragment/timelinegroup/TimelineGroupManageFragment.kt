package tech.ketc.numeri.ui.fragment.timelinegroup

import android.app.Dialog
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import org.jetbrains.anko.*
import org.jetbrains.anko.design.textInputLayout
import tech.ketc.numeri.R
import tech.ketc.numeri.infra.entity.TimelineGroup
import tech.ketc.numeri.ui.activity.timelinemanage.OnAddFabClickListener
import tech.ketc.numeri.ui.components.RecyclerUIComponent
import tech.ketc.numeri.ui.components.IRecyclerUIComponent
import tech.ketc.numeri.ui.fragment.dialog.MessageDialogFragment
import tech.ketc.numeri.ui.fragment.dialog.OnDialogItemSelectedListener
import tech.ketc.numeri.ui.model.TimelineManageViewModel
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.android.act
import tech.ketc.numeri.util.android.pref
import tech.ketc.numeri.util.android.ui.recycler.SimpleItemTouchHelper
import tech.ketc.numeri.util.android.ui.recycler.SimpleRecyclerAdapter
import tech.ketc.numeri.util.anko.marginBottom
import tech.ketc.numeri.util.anko.marginTop
import tech.ketc.numeri.util.anko.textInputlparams
import tech.ketc.numeri.util.arch.owner.bindLaunch
import tech.ketc.numeri.util.arch.viewmodel.commonViewModel
import tech.ketc.numeri.util.di.AutoInject
import tech.ketc.numeri.util.logTag
import javax.inject.Inject

class TimelineGroupManageFragment : Fragment(), AutoInject,
        IRecyclerUIComponent by RecyclerUIComponent(), OnAddFabClickListener, OnDialogItemSelectedListener {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: TimelineManageViewModel by commonViewModel { mViewModelFactory }

    private val mAdapter = SimpleRecyclerAdapter<TimelineGroup>({ it.name }, {
        (act as? OnGroupSelectedListener)?.onGroupSelected(it)
    })
    private var mReserveDeleteGroupName: String? = null
    private var mReserveDeleteGroupPosition: Int = -1
    private var mDeleteConfirmEnabled = true

    companion object {
        fun create() = TimelineGroupManageFragment()
        private val TAG_GROUP_CREATE = "TAG_GROUP_CREATE"
        private val TAG_GROUP_DELETE = "TAG_GROUP_DELETE"
        private val REQUEST_CONFIRM_DELETE = 100
        private val EXTRA_RESERVE_DELETE_GROUP_NAME = "EXTRA_RESERVE_DELETE_GROUP_NAME"
        private val EXTRA_RESERVE_DELETE_GROUP_POSITION = "EXTRA_RESERVE_DELETE_GROUP_POSITION"
        private val PREF_DELETE_CONFIRM = "PREF_DELETE_CONFIRM"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDeleteConfirmEnabled = act.pref.getBoolean(PREF_DELETE_CONFIRM, true)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = createView(act)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            mReserveDeleteGroupName = it.getString(EXTRA_RESERVE_DELETE_GROUP_NAME)
        }
        initialize()
    }

    private fun initialize() {
        recycler.adapter = mAdapter
        bindLaunch {
            val groups = mModel.loadGroupList().await().result
            Logger.v(logTag, groups.joinToString { it.name })
            mAdapter.values.addAll(groups)
            mAdapter.notifyDataSetChanged()
            initializeUIBehavior()
        }
    }

    private fun initializeUIBehavior() {
        val helper = SimpleItemTouchHelper(swipeEnable = true, onSwiped = { viewHolder, _ ->
            val position = viewHolder.adapterPosition
            val values = mAdapter.values
            if (position <= values.lastIndex) {
                val groupName = values[position].name
                mReserveDeleteGroupName = groupName
                mReserveDeleteGroupPosition = position
                if (mDeleteConfirmEnabled)
                    showConfirmDeleteDialog(groupName)
                else
                    deleteGroup(groupName)
            }
        })
        helper.attachToRecyclerView(recycler)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.group_manage, menu)
        menu.findItem(R.id.check_delete_confirm).isChecked = mDeleteConfirmEnabled
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.check_delete_confirm -> {
                act.pref.edit()
                        .putBoolean(PREF_DELETE_CONFIRM, !mDeleteConfirmEnabled)
                        .apply()
                mDeleteConfirmEnabled = !mDeleteConfirmEnabled
                item.isChecked = mDeleteConfirmEnabled
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }


    private fun showGroupCreateDialog() = GroupCreateDialogFragment().show(childFragmentManager, TAG_GROUP_CREATE)

    private fun check(groupName: String) = !mAdapter.values.any { it.name == groupName }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(EXTRA_RESERVE_DELETE_GROUP_NAME, mReserveDeleteGroupName)
        outState.putInt(EXTRA_RESERVE_DELETE_GROUP_POSITION, mReserveDeleteGroupPosition)
        super.onSaveInstanceState(outState)
    }

    private fun showConfirmDeleteDialog(groupName: String) {
        mReserveDeleteGroupName = groupName
        MessageDialogFragment
                .create(REQUEST_CONFIRM_DELETE,
                        "${getString(R.string.message_confirm_group_delete)}[$groupName]",
                        positiveId = R.string.delete).show(childFragmentManager, TAG_GROUP_DELETE)
    }

    private fun onPositiveClick(name: String) {
        bindLaunch {
            val result = mModel.createGroup(name).await().result
            val values = mAdapter.values
            values.add(result)
            mAdapter.notifyItemInserted(values.size)
        }
    }

    private fun postDelete() {
        mReserveDeleteGroupPosition = -1
        mReserveDeleteGroupName = null
    }

    private fun deleteGroup(groupName: String) {
        bindLaunch {
            mModel.deleteGroup(TimelineGroup(groupName)).await().ifError { throw it }
            Logger.v(logTag, "${mAdapter.values.size},$mReserveDeleteGroupPosition")
            mAdapter.values.removeAt(mReserveDeleteGroupPosition)
            mAdapter.notifyItemRemoved(mReserveDeleteGroupPosition)
            postDelete()
        }
    }

    //impl interface
    override fun onDialogItemSelected(requestCode: Int, itemId: Int) {
        if (requestCode != REQUEST_CONFIRM_DELETE) return
        fun cancel() {
            mAdapter.notifyItemChanged(mReserveDeleteGroupPosition)
            postDelete()
        }
        when (itemId) {
            R.string.delete -> {
                deleteGroup(mReserveDeleteGroupName!!)
            }
            R.string.cancel -> {
                cancel()
            }
            MessageDialogFragment.CANCEL -> {
                cancel()
            }
        }
    }

    override fun onAddFabClick() = showGroupCreateDialog()

    class GroupCreateDialogFragment : DialogFragment(), TextWatcher {
        private val parent by lazy { parentFragment as TimelineGroupManageFragment }
        private lateinit var editText: EditText

        companion object {
            private val LIMIT_NAME_SIZE = 15
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val view = context!!.relativeLayout {
                textInputLayout {
                    editText {
                        id = R.id.text_input
                        inputType = InputType.TYPE_CLASS_TEXT
                        singleLine = true
                        hint = context.getString(R.string.hint_input_timeline_group_name)
                        gravity = Gravity.TOP or Gravity.START
                    }.textInputlparams(matchParent, wrapContent)
                }.lparams(matchParent, wrapContent) {
                    marginTop = dimen(R.dimen.margin_medium)
                    marginEnd = dimen(R.dimen.margin_large)
                    marginStart = dimen(R.dimen.margin_large)
                    marginBottom = dimen(R.dimen.margin_medium)
                }
            }
            editText = view.findViewById(R.id.text_input)

            val dialog = AlertDialog.Builder(act)
                    .setMessage(R.string.dialog_message_add_timeline_group)
                    .setView(view)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.add) { _, _ ->
                        val name = editText.text.toString()
                        parent.onPositiveClick(name)
                    }.create()
            editText.addTextChangedListener(this)
            return dialog
        }

        override fun onResume() {
            super.onResume()
            onTextChanged(editText.text, 0, 0, 0)
        }

        override fun afterTextChanged(text: Editable) {
        }

        override fun beforeTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {
            val button = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            when {
                text.length > 15 || text.isEmpty() -> {
                    editText.error = "1 ~ $LIMIT_NAME_SIZE${getString(R.string.error_enter_in_range_up_to)}"
                    button.isEnabled = false
                }
                !parent.check(text.toString()) -> {
                    editText.error = getString(R.string.error_group_name_must_be_unique)
                    button.isEnabled = false
                }
                else -> {
                    editText.error = null
                    button.isEnabled = true
                }
            }
        }
    }
}
