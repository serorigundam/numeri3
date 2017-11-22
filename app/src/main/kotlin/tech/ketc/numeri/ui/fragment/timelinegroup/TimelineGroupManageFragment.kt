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
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import org.jetbrains.anko.*
import org.jetbrains.anko.design.textInputLayout
import tech.ketc.numeri.R
import tech.ketc.numeri.infra.entity.TimelineGroup
import tech.ketc.numeri.ui.activity.timelinemanage.OnAddFabClickListener
import tech.ketc.numeri.ui.components.RecyclerUIComponent
import tech.ketc.numeri.ui.components.IRecyclerUIComponent
import tech.ketc.numeri.ui.model.TimelineManageViewModel
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.android.act
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
        IRecyclerUIComponent by RecyclerUIComponent(), OnAddFabClickListener {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: TimelineManageViewModel by commonViewModel { mViewModelFactory }

    private val mAdapter = SimpleRecyclerAdapter<TimelineGroup>({ it.name }, {
        (act as? OnGroupSelectedListener)?.onGroupSelected(it)
    })

    companion object {
        fun create() = TimelineGroupManageFragment()
        private val TAG_GROUP_CREATE = "TAG_GROUP_CREATE"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = createView(act)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize() {
        recycler.adapter = mAdapter
        bindLaunch {
            val groups = mModel.loadGroupList().await().result
            Logger.v(logTag, groups.joinToString { it.name })
            mAdapter.values.addAll(groups)
            mAdapter.notifyDataSetChanged()
        }
    }

    private fun showGroupCreateDialog() = GroupCreateDialogFragment().show(childFragmentManager, TAG_GROUP_CREATE)


    override fun onAddFabClick() = showGroupCreateDialog()

    private fun check(groupName: String) = !mAdapter.values.any { it.name == groupName }


    private fun onPositiveClick(name: String) {
        bindLaunch {
            val result = mModel.createGroup(name).await().result
            val values = mAdapter.values
            values.add(result)
            mAdapter.notifyItemInserted(values.size)
        }
    }

    class GroupCreateDialogFragment : DialogFragment() {
        private val parent by lazy { parentFragment as TimelineGroupManageFragment }

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
                        hint = context.getString(R.string.hint_input_timline_group_name)
                        gravity = Gravity.TOP or Gravity.START
                    }.textInputlparams(matchParent, wrapContent)
                }.lparams(matchParent, wrapContent) {
                    marginTop = dimen(R.dimen.margin_medium)
                    marginEnd = dimen(R.dimen.margin_large)
                    marginStart = dimen(R.dimen.margin_large)
                    marginBottom = dimen(R.dimen.margin_medium)
                }
            }
            val editText: EditText = view.findViewById(R.id.text_input)

            val dialog = AlertDialog.Builder(act)
                    .setMessage(R.string.dialog_message_add_timeline_group)
                    .setView(view)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.add) { _, _ ->
                        val name = editText.text.toString()
                        parent.onPositiveClick(name)
                    }.create()
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(text: Editable) {
                }

                override fun beforeTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {
                    val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    when {
                        text.length > 15 -> {
                            editText.error = "$LIMIT_NAME_SIZE${getString(R.string.error_enter_in_range_up_to)}"
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
            })
            return dialog
        }
    }
}
