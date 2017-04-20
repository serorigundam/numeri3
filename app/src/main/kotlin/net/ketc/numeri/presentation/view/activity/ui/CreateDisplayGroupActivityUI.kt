package net.ketc.numeri.presentation.view.activity.ui

import android.support.v7.widget.Toolbar
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import net.ketc.numeri.R
import net.ketc.numeri.presentation.view.activity.CreateDisplayGroupActivity
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.textInputLayout

class CreateDisplayGroupActivityUI : AnkoComponent<CreateDisplayGroupActivity> {

    lateinit var groupNameEdit: EditText
        private set
    lateinit var toolbar: Toolbar
        private set

    override fun createView(ui: AnkoContext<CreateDisplayGroupActivity>) = with(ui) {
        linearLayout {
            orientation = LinearLayout.VERTICAL
            lparams(matchParent, matchParent)

            appBarLayout {
                id = R.id.app_bar
                toolbar = toolbar {
                    id = R.id.toolbar
                }.lparams(matchParent, wrapContent)
            }.lparams(matchParent, wrapContent)

            textInputLayout {
                groupNameEdit = editText {
                    id = R.id.column_group_name_edit
                    lines = 1
                    inputType = InputType.TYPE_CLASS_TEXT
                    hint = context.getString(R.string.hint_input_column_group_name)
                }
            }.lparams(matchParent, wrapContent) {
                margin = dimen(R.dimen.margin_medium)
            }
        }
    }
}